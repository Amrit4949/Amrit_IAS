package com.amrit.beacon

import android.app.Application
import android.content.Context
import com.amrit.beacon.alert.AlertEngine
import com.amrit.beacon.data.SettingsStore
import com.amrit.beacon.net.LanTransport
import com.amrit.beacon.service.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Hand-rolled dependency container.
 *
 * Deliberately not Hilt: there are three long-lived objects, they all take a [Context] and
 * nothing else, and the whole graph fits on a screen. Adding an annotation processor to
 * express that would be more machinery than the problem has.
 *
 * The important property is that these are *singletons across the process*. The alert engine
 * in particular must be the same instance for the service that starts an alert and the
 * activity whose stop button ends it — two instances would mean a stop button that stops
 * nothing.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settingsStore = SettingsStore(appContext)
    val alertEngine = AlertEngine(appContext)
    val transport = LanTransport(appContext)
}

class BeaconApp : Application() {

    lateinit var container: AppContainer
        private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Notifications.createChannels(this)
        scope.launch { container.settingsStore.ensureIdentity() }
    }

    companion object {
        fun container(context: Context): AppContainer =
            (context.applicationContext as BeaconApp).container
    }
}
