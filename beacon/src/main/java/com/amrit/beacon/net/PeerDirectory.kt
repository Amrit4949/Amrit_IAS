package com.amrit.beacon.net

/**
 * One phone, however many ways there are to reach it.
 *
 * The same handset shows up twice — once from mDNS on the local network, once from the
 * relay's registration list — and the user must never see it twice. Merging on device id and
 * carrying *both* routes on one row is what lets a single Ring button pick the best path
 * without asking anyone to understand the difference.
 */
data class DirectoryEntry(
    val deviceId: String,
    val deviceName: String,
    /** Non-null when this phone is on the same network right now. */
    val lan: Peer?,
    /** True when this phone has a live push registration with the relay. */
    val reachableRemotely: Boolean,
) {
    val isNearby: Boolean get() = lan != null

    /** Nothing to ring: seen once, reachable by neither route now. */
    val isUnreachable: Boolean get() = lan == null && !reachableRemotely
}

object PeerDirectory {

    /**
     * Merges the two peer sources.
     *
     * LAN wins on naming when both are present: that name came off an authenticated HELLO
     * with the device itself moments ago, whereas the relay's copy is whatever was sealed at
     * the last registration and may predate a rename.
     */
    fun merge(
        lanPeers: List<Peer>,
        cloudPeers: List<CloudTransport.CloudPeer>,
    ): List<DirectoryEntry> {
        val byId = LinkedHashMap<String, DirectoryEntry>()

        for (peer in lanPeers) {
            byId[peer.deviceId] = DirectoryEntry(
                deviceId = peer.deviceId,
                deviceName = peer.deviceName,
                lan = peer,
                reachableRemotely = false,
            )
        }
        for (peer in cloudPeers) {
            val existing = byId[peer.deviceId]
            byId[peer.deviceId] = if (existing == null) {
                DirectoryEntry(
                    deviceId = peer.deviceId,
                    deviceName = peer.deviceName,
                    lan = null,
                    reachableRemotely = true,
                )
            } else {
                existing.copy(reachableRemotely = true)
            }
        }

        // Nearby first: if a phone is in the same building, that is almost always the one
        // being looked for, and it is the route that rings fastest.
        return byId.values.sortedWith(
            compareByDescending<DirectoryEntry> { it.isNearby }
                .thenBy { it.deviceName.lowercase() }
        )
    }
}
