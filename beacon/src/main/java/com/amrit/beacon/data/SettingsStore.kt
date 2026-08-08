package com.amrit.beacon.data

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amrit.beacon.alert.AlertProfile
import com.amrit.beacon.net.Crypto
import com.amrit.beacon.net.PairCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Everything the app remembers between launches, which is deliberately very little: who this
 * phone says it is, and which circle it belongs to.
 *
 * There is no account, no server-side record and no contact list. The circle code is the
 * only thing that links two phones together, and it never leaves the device — only HMACs
 * derived from it go on the wire.
 */
data class BeaconSettings(
    val deviceId: String,
    val deviceName: String,
    /** `null` until the user joins or creates a circle. */
    val pairCode: String?,
    /** Whether this phone should stay reachable, i.e. run the listener service. */
    val listening: Boolean,
    val defaultProfile: AlertProfile,
    /**
     * HTTPS base URL of the relay that carries rings when the phones are not on the same
     * network. Empty means local-network-only, which is a perfectly valid way to run the app.
     */
    val relayUrl: String,
) {
    val isPaired: Boolean get() = pairCode != null

    val hasRelay: Boolean get() = relayUrl.isNotBlank()
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "beacon")

class SettingsStore(private val context: Context) {

    private object Keys {
        val DEVICE_ID = stringPreferencesKey("device_id")
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val PAIR_CODE = stringPreferencesKey("pair_code")
        val LISTENING = booleanPreferencesKey("listening")
        val DEFAULT_PROFILE = stringPreferencesKey("default_profile")
        val RELAY_URL = stringPreferencesKey("relay_url")
    }

    val settings: Flow<BeaconSettings> = context.dataStore.data.map { prefs ->
        BeaconSettings(
            deviceId = prefs[Keys.DEVICE_ID].orEmpty(),
            deviceName = prefs[Keys.DEVICE_NAME] ?: defaultDeviceName(),
            pairCode = prefs[Keys.PAIR_CODE]?.takeIf { PairCode.isComplete(it) },
            listening = prefs[Keys.LISTENING] ?: true,
            defaultProfile = AlertProfile.fromWireName(
                prefs[Keys.DEFAULT_PROFILE] ?: AlertProfile.DEFAULT.wireName
            ),
            relayUrl = prefs[Keys.RELAY_URL].orEmpty(),
        )
    }

    suspend fun current(): BeaconSettings = settings.first()

    /**
     * Assigns this install a random id the first time it runs. Random rather than derived
     * from hardware: an id that survives a reinstall would be a tracking identifier, and
     * nothing here needs one.
     */
    suspend fun ensureIdentity(): BeaconSettings {
        context.dataStore.edit { prefs ->
            if (prefs[Keys.DEVICE_ID].isNullOrEmpty()) prefs[Keys.DEVICE_ID] = Crypto.newDeviceId()
            if (prefs[Keys.DEVICE_NAME].isNullOrBlank()) prefs[Keys.DEVICE_NAME] = defaultDeviceName()
        }
        return current()
    }

    suspend fun setDeviceName(name: String) {
        val cleaned = name.trim().take(MAX_NAME_LENGTH).ifBlank { defaultDeviceName() }
        context.dataStore.edit { it[Keys.DEVICE_NAME] = cleaned }
    }

    /** Stores the circle code in canonical form so both phones derive an identical key. */
    suspend fun setPairCode(code: String) {
        val normalized = PairCode.normalize(code)
        require(normalized.length == PairCode.LENGTH) { "pair code must be ${PairCode.LENGTH} characters" }
        context.dataStore.edit { it[Keys.PAIR_CODE] = normalized }
    }

    suspend fun leaveCircle() {
        context.dataStore.edit { it.remove(Keys.PAIR_CODE) }
    }

    suspend fun setListening(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LISTENING] = enabled }
    }

    /**
     * Stores the relay URL, or clears it when blank. Rejects anything that is not HTTPS: the
     * request carries a push token and a circle id, neither of which should ever be sent in
     * the clear.
     */
    suspend fun setRelayUrl(url: String) {
        val cleaned = url.trim().trimEnd('/')
        require(cleaned.isEmpty() || cleaned.startsWith("https://")) {
            "relay URL must start with https://"
        }
        context.dataStore.edit { prefs ->
            if (cleaned.isEmpty()) prefs.remove(Keys.RELAY_URL) else prefs[Keys.RELAY_URL] = cleaned
        }
    }

    suspend fun setDefaultProfile(profile: AlertProfile) {
        context.dataStore.edit { it[Keys.DEFAULT_PROFILE] = profile.wireName }
    }

    private fun defaultDeviceName(): String {
        val model = Build.MODEL?.trim().orEmpty()
        val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
        val name = when {
            model.isEmpty() -> manufacturer.ifEmpty { "Android phone" }
            manufacturer.isNotEmpty() && !model.startsWith(manufacturer, ignoreCase = true) ->
                "$manufacturer $model"

            else -> model
        }
        return name.replaceFirstChar(Char::uppercase).take(MAX_NAME_LENGTH)
    }

    companion object {
        const val MAX_NAME_LENGTH = 40
    }
}
