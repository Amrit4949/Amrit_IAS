package com.amrit.beacon.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Finds the other phones in the circle over the local network and carries signed messages
 * between them.
 *
 * Why the LAN and not a push server: it needs no account, no backend to keep running, no
 * Firebase project, and no per-message cost — you install the APK on two phones, type the
 * same code, and it works. The cost is that both phones must be on the same Wi-Fi. See
 * `beacon/README.md` for how the same [Wire] protocol extends over FCM when they are not.
 *
 * Discovery is mDNS via [NsdManager] (`_beacon._tcp`), and every discovered service is
 * probed with a signed HELLO before it is shown, so a stranger on the same café Wi-Fi
 * running this app never appears in your list and can never make your phone scream.
 */
class LanTransport(private val context: Context) {

    fun interface MessageHandler {
        /** Invoked off the main thread for every authenticated, non-replayed message. */
        fun onMessage(message: Wire.Message, from: InetAddress)
    }

    enum class Status { STOPPED, STARTING, RUNNING, FAILED }

    sealed interface SendResult {
        data class Ok(val peerName: String) : SendResult
        data class Failed(val reason: String) : SendResult
    }

    private val nsdManager: NsdManager? = context.getSystemService(NsdManager::class.java)
    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(WifiManager::class.java)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val replayGuard = ReplayGuard()

    /** NSD can only resolve one service at a time on many builds; serialise every resolve. */
    private val resolveMutex = Mutex()
    private val resolveQueue = Channel<NsdServiceInfo>(capacity = Channel.BUFFERED)

    private val _peers = MutableStateFlow<List<Peer>>(emptyList())
    val peers: StateFlow<List<Peer>> = _peers.asStateFlow()

    private val _status = MutableStateFlow(Status.STOPPED)
    val status: StateFlow<Status> = _status.asStateFlow()

    private var identity: Identity? = null
    private var circleKey: ByteArray? = null
    private var handler: MessageHandler? = null

    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null
    private var resolveJob: Job? = null
    private var pruneJob: Job? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    @Synchronized
    fun start(identity: Identity, circleKey: ByteArray, handler: MessageHandler) {
        if (_status.value == Status.RUNNING || _status.value == Status.STARTING) return
        val nsd = nsdManager ?: run {
            Log.e(TAG, "no NsdManager on this device")
            _status.value = Status.FAILED
            return
        }

        this.identity = identity
        this.circleKey = circleKey
        this.handler = handler
        _status.value = Status.STARTING

        val socket = try {
            ServerSocket(0).apply { reuseAddress = true }
        } catch (e: Exception) {
            Log.e(TAG, "could not open listening socket", e)
            _status.value = Status.FAILED
            return
        }
        serverSocket = socket

        acquireMulticastLock()
        acceptJob = scope.launch { acceptLoop(socket) }
        resolveJob = scope.launch { resolveLoop() }
        pruneJob = scope.launch { pruneLoop() }
        register(nsd, identity, socket.localPort)
        discover(nsd)

        _status.value = Status.RUNNING
        Log.i(TAG, "listening on port ${socket.localPort} as ${identity.deviceName}")
    }

    @Synchronized
    fun stop() {
        val nsd = nsdManager
        registrationListener?.let { l -> runCatching { nsd?.unregisterService(l) } }
        registrationListener = null
        discoveryListener?.let { l -> runCatching { nsd?.stopServiceDiscovery(l) } }
        discoveryListener = null

        acceptJob?.cancel()
        acceptJob = null
        resolveJob?.cancel()
        resolveJob = null
        pruneJob?.cancel()
        pruneJob = null
        // Closing the socket is what actually unblocks accept(); cancelling the coroutine
        // alone would not, because accept() is not interruptible.
        runCatching { serverSocket?.close() }
        serverSocket = null

        releaseMulticastLock()
        _peers.value = emptyList()
        identity = null
        circleKey = null
        handler = null
        _status.value = Status.STOPPED
    }

    // ---------------------------------------------------------------- inbound

    private suspend fun acceptLoop(socket: ServerSocket) {
        while (!socket.isClosed) {
            val client = try {
                withContext(Dispatchers.IO) { socket.accept() }
            } catch (e: Exception) {
                if (!socket.isClosed) Log.w(TAG, "accept failed", e)
                return
            }
            // One coroutine per connection so a peer that opens a socket and then says
            // nothing cannot block everyone else behind it.
            scope.launch { serveClient(client) }
        }
    }

    private fun serveClient(client: Socket) {
        client.use { sock ->
            try {
                sock.soTimeout = READ_TIMEOUT_MILLIS
                val key = circleKey ?: return
                val me = identity ?: return

                val line = BufferedReader(InputStreamReader(sock.getInputStream(), Charsets.UTF_8))
                    .readLine() ?: return

                when (val decoded = Wire.decode(line, key)) {
                    is Wire.Decoded.Rejected -> {
                        // Never tell the peer why. A caller who knows whether the MAC or the
                        // timestamp failed learns something about the key; a caller who just
                        // sees the socket close learns nothing.
                        Log.i(TAG, "dropped message from ${sock.inetAddress}: ${decoded.reason}")
                        return
                    }

                    is Wire.Decoded.Ok -> {
                        val message = decoded.message
                        if (message.deviceId == me.deviceId) return // our own advertisement
                        when (val verdict = replayGuard.check(message)) {
                            is ReplayGuard.Verdict.Reject -> {
                                Log.i(TAG, "dropped replayed message: ${verdict.reason}")
                                return
                            }

                            ReplayGuard.Verdict.Accept -> Unit
                        }

                        // Answer before acting, so the sender's UI confirms quickly rather
                        // than waiting on the alert machinery to spin up.
                        writeHello(sock, me, key)
                        handler?.onMessage(message, sock.inetAddress)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "error serving ${client.inetAddress}", e)
            }
        }
    }

    private fun writeHello(sock: Socket, me: Identity, key: ByteArray) {
        val reply = Wire.Message(
            verb = Wire.Verb.HELLO,
            deviceId = me.deviceId,
            deviceName = me.deviceName,
            nonce = Crypto.newNonce(),
            sentAtMillis = System.currentTimeMillis(),
            profile = AlertProfileWire.NONE,
        )
        runCatching {
            sock.getOutputStream().apply {
                write(reply.encode(key).toByteArray(Charsets.UTF_8))
                flush()
            }
        }
    }

    // ---------------------------------------------------------------- outbound

    /**
     * Sends one signed message to [peer] and waits for its signed reply. The reply is what
     * makes this meaningful: it proves the phone that acted on the request is the phone we
     * meant, and not something that merely accepted the TCP connection.
     */
    suspend fun send(peer: Peer, verb: Wire.Verb, profile: String): SendResult =
        withContext(Dispatchers.IO) {
            val me = identity ?: return@withContext SendResult.Failed("not started")
            val key = circleKey ?: return@withContext SendResult.Failed("no circle key")

            val message = Wire.Message(
                verb = verb,
                deviceId = me.deviceId,
                deviceName = me.deviceName,
                nonce = Crypto.newNonce(),
                sentAtMillis = System.currentTimeMillis(),
                profile = profile,
            )

            try {
                Socket().use { sock ->
                    sock.connect(InetSocketAddress(peer.address, peer.port), CONNECT_TIMEOUT_MILLIS)
                    sock.soTimeout = READ_TIMEOUT_MILLIS
                    sock.getOutputStream().apply {
                        write(message.encode(key).toByteArray(Charsets.UTF_8))
                        flush()
                    }
                    val reply = BufferedReader(
                        InputStreamReader(sock.getInputStream(), Charsets.UTF_8)
                    ).readLine() ?: return@withContext SendResult.Failed("no reply")

                    when (val decoded = Wire.decode(reply, key)) {
                        is Wire.Decoded.Ok -> SendResult.Ok(decoded.message.deviceName)
                        is Wire.Decoded.Rejected -> SendResult.Failed("peer is not in this circle")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "send to ${peer.deviceName} failed", e)
                SendResult.Failed(e.message ?: "network error")
            }
        }

    // ---------------------------------------------------------------- discovery

    private fun register(nsd: NsdManager, identity: Identity, port: Int) {
        val info = NsdServiceInfo().apply {
            // The id travels in the service name so we can filter our own advertisement out
            // of discovery without a round trip. The friendly name does not: it can contain
            // anything, and it is authenticated during the HELLO exchange instead.
            serviceName = SERVICE_PREFIX + identity.deviceId
            serviceType = SERVICE_TYPE
            this.port = port
        }
        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.i(TAG, "registered as ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "registration failed with $errorCode")
                _status.value = Status.FAILED
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) = Unit
            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) = Unit
        }
        registrationListener = listener
        runCatching { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, listener) }
            .onFailure {
                Log.e(TAG, "could not register service", it)
                _status.value = Status.FAILED
            }
    }

    private fun discover(nsd: NsdManager) {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onServiceFound(info: NsdServiceInfo) {
                if (!info.serviceName.startsWith(SERVICE_PREFIX)) return
                if (info.serviceName.removePrefix(SERVICE_PREFIX) == identity?.deviceId) return
                resolveQueue.trySend(info)
            }

            override fun onServiceLost(info: NsdServiceInfo) {
                val id = info.serviceName.removePrefix(SERVICE_PREFIX)
                _peers.value = _peers.value.filterNot { it.deviceId == id }
            }

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "discovery failed to start ($errorCode)")
                _status.value = Status.FAILED
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }
        discoveryListener = listener
        runCatching { nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener) }
            .onFailure { Log.e(TAG, "could not start discovery", it) }
    }

    private suspend fun resolveLoop() {
        for (info in resolveQueue) {
            resolveMutex.withLock {
                val resolved = withTimeoutOrNull(RESOLVE_TIMEOUT_MILLIS) { resolve(info) }
                if (resolved != null) probeAndAdd(resolved)
            }
        }
    }

    private suspend fun resolve(info: NsdServiceInfo): NsdServiceInfo? {
        val nsd = nsdManager ?: return null
        val result = Channel<NsdServiceInfo?>(capacity = 1)
        val listener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                result.trySend(null)
            }

            override fun onServiceResolved(info: NsdServiceInfo) {
                result.trySend(info)
            }
        }
        @Suppress("DEPRECATION")
        runCatching { nsd.resolveService(info, listener) }
            .onFailure { return null }
        return result.receive()
    }

    /**
     * Confirms a resolved service really is one of ours before it becomes a tappable row.
     * A phone running Beacon with a *different* circle code will fail the HELLO exchange and
     * is silently dropped — which is exactly the behaviour you want on a shared network.
     */
    private suspend fun probeAndAdd(info: NsdServiceInfo) {
        @Suppress("DEPRECATION")
        val address: InetAddress = info.host ?: return
        val id = info.serviceName.removePrefix(SERVICE_PREFIX)
        if (id == identity?.deviceId) return

        val candidate = Peer(
            deviceId = id,
            deviceName = id,
            address = address,
            port = info.port,
            lastSeenElapsedMillis = SystemClock.elapsedRealtime(),
        )
        when (val result = send(candidate, Wire.Verb.HELLO, AlertProfileWire.NONE)) {
            is SendResult.Ok -> {
                val peer = candidate.copy(
                    deviceName = result.peerName.ifBlank { id },
                    lastSeenElapsedMillis = SystemClock.elapsedRealtime(),
                )
                _peers.value = (_peers.value.filterNot { it.deviceId == peer.deviceId } + peer)
                    .sortedBy { it.deviceName.lowercase() }
            }

            is SendResult.Failed -> Log.i(TAG, "ignoring $id: ${result.reason}")
        }
    }

    /**
     * `onServiceLost` is best-effort — a phone that goes flat, or walks out of Wi-Fi range,
     * often never produces one. Re-probing on a timer keeps the list honest, so a row you can
     * see is a row you can actually ring.
     */
    private suspend fun pruneLoop() {
        while (true) {
            kotlinx.coroutines.delay(PRUNE_INTERVAL_MILLIS)
            val snapshot = _peers.value
            for (peer in snapshot) {
                if (send(peer, Wire.Verb.HELLO, AlertProfileWire.NONE) is SendResult.Ok) {
                    _peers.value = _peers.value.map {
                        if (it.deviceId == peer.deviceId) {
                            it.copy(lastSeenElapsedMillis = SystemClock.elapsedRealtime())
                        } else {
                            it
                        }
                    }
                } else {
                    _peers.value = _peers.value.filterNot { it.deviceId == peer.deviceId }
                }
            }
        }
    }

    // ---------------------------------------------------------------- misc

    /**
     * Some Wi-Fi chipsets drop multicast when the screen is off to save power, which is
     * exactly when a lost phone needs to be discoverable. The lock costs battery; it is held
     * only while the listener is running.
     */
    private fun acquireMulticastLock() {
        if (multicastLock != null) return
        multicastLock = runCatching {
            wifiManager?.createMulticastLock("beacon-mdns")?.apply {
                setReferenceCounted(false)
                acquire()
            }
        }.getOrNull()
    }

    private fun releaseMulticastLock() {
        runCatching { multicastLock?.takeIf { it.isHeld }?.release() }
        multicastLock = null
    }

    companion object {
        private const val TAG = "BeaconLan"
        const val SERVICE_TYPE = "_beacon._tcp."
        const val SERVICE_PREFIX = "beacon-"
        private const val CONNECT_TIMEOUT_MILLIS = 2_500
        private const val READ_TIMEOUT_MILLIS = 3_000
        private const val RESOLVE_TIMEOUT_MILLIS = 6_000L
        private const val PRUNE_INTERVAL_MILLIS = 30_000L
    }
}

/** Profile slot values used by messages that are not a ring request. */
object AlertProfileWire {
    const val NONE = "none"
}
