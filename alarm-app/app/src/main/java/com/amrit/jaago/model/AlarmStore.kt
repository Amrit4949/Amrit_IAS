package com.amrit.jaago.model

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Everything is kept in one SharedPreferences file as JSON. There are at most a
 * few dozen alarms, so a database would only add build complexity.
 *
 * init() is called from JaagoApp.onCreate(), which Android always runs before any
 * activity, service or broadcast receiver in this process.
 */
object AlarmStore {

    private const val FILE = "jaago_store"
    private const val KEY_ALARMS = "alarms"
    private const val KEY_NEXT_ID = "next_id"
    private const val KEY_HISTORY = "history"
    private const val KEY_PASSAGES = "custom_passages"
    private const val KEY_USE_CUSTOM = "use_custom_passages"
    private const val SNOOZE_PREFIX = "snoozes_"
    private const val HISTORY_LIMIT = 60

    private lateinit var prefs: SharedPreferences

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _history = MutableStateFlow<List<WakeEvent>>(emptyList())
    val history: StateFlow<List<WakeEvent>> = _history.asStateFlow()

    private val _customPassages = MutableStateFlow<List<String>>(emptyList())
    val customPassages: StateFlow<List<String>> = _customPassages.asStateFlow()

    val isReady: Boolean get() = this::prefs.isInitialized

    fun init(context: Context) {
        if (isReady) return
        prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        _alarms.value = readAlarms()
        _history.value = readHistory()
        _customPassages.value = readPassages()
    }

    // ----- alarms -----

    private fun readAlarms(): List<Alarm> {
        val raw = prefs.getString(KEY_ALARMS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<Alarm>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { out.add(AlarmJson.alarmFromJson(it)) }
            }
            out.sortedWith(compareBy({ it.hour }, { it.minute }))
        } catch (t: Throwable) {
            emptyList()
        }
    }

    private fun writeAlarms(list: List<Alarm>) {
        val arr = JSONArray()
        list.forEach { arr.put(AlarmJson.alarmToJson(it)) }
        prefs.edit().putString(KEY_ALARMS, arr.toString()).apply()
        _alarms.value = list.sortedWith(compareBy({ it.hour }, { it.minute }))
    }

    fun all(): List<Alarm> = _alarms.value

    fun get(id: Int): Alarm? = _alarms.value.firstOrNull { it.id == id }

    fun upsert(alarm: Alarm) {
        val current = _alarms.value.toMutableList()
        val index = current.indexOfFirst { it.id == alarm.id }
        if (index >= 0) current[index] = alarm else current.add(alarm)
        writeAlarms(current)
    }

    fun delete(id: Int) {
        writeAlarms(_alarms.value.filterNot { it.id == id })
        prefs.edit().remove(SNOOZE_PREFIX + id).remove(OVERRIDE_PREFIX + id).apply()
    }

    fun newId(): Int {
        val next = prefs.getInt(KEY_NEXT_ID, 1)
        prefs.edit().putInt(KEY_NEXT_ID, next + 1).apply()
        return next
    }

    // ----- snooze bookkeeping (survives the process being killed between snoozes) -----

    fun snoozesUsed(alarmId: Int): Int = prefs.getInt(SNOOZE_PREFIX + alarmId, 0)

    fun noteSnoozeUsed(alarmId: Int) {
        prefs.edit().putInt(SNOOZE_PREFIX + alarmId, snoozesUsed(alarmId) + 1).apply()
    }

    fun resetSnoozes(alarmId: Int) {
        prefs.edit().remove(SNOOZE_PREFIX + alarmId).apply()
    }

    // ----- one-off firing times (snooze, "test in 1 minute") -----
    //
    // A snooze or a test re-arms the alarm's own PendingIntent at an unusual time.
    // If the process restarts before it fires, rescheduleAll() must put back that
    // time, not the alarm's regular one, or the snooze is silently lost.

    private const val OVERRIDE_PREFIX = "override_at_"

    fun overrideAt(alarmId: Int): Long = prefs.getLong(OVERRIDE_PREFIX + alarmId, 0L)

    fun setOverride(alarmId: Int, at: Long) {
        prefs.edit().putLong(OVERRIDE_PREFIX + alarmId, at).apply()
    }

    fun clearOverride(alarmId: Int) {
        prefs.edit().remove(OVERRIDE_PREFIX + alarmId).apply()
    }

    // ----- setup gate -----

    var setupDismissed: Boolean
        get() = isReady && prefs.getBoolean("setup_dismissed", false)
        set(value) {
            if (isReady) prefs.edit().putBoolean("setup_dismissed", value).apply()
        }

    /** The manufacturer auto-start screen cannot be read back, so the user ticks it. */
    var oemStepConfirmed: Boolean
        get() = isReady && prefs.getBoolean("oem_step_confirmed", false)
        set(value) {
            if (isReady) prefs.edit().putBoolean("oem_step_confirmed", value).apply()
        }

    // ----- wake-up log -----

    private fun readHistory(): List<WakeEvent> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = ArrayList<WakeEvent>(arr.length())
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { out.add(AlarmJson.eventFromJson(it)) }
            }
            out
        } catch (t: Throwable) {
            emptyList()
        }
    }

    fun logEvent(event: WakeEvent) {
        if (!isReady) return
        val list = (listOf(event) + _history.value).take(HISTORY_LIMIT)
        val arr = JSONArray()
        list.forEach { arr.put(AlarmJson.eventToJson(it)) }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
        _history.value = list
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
        _history.value = emptyList()
    }

    // ----- user supplied typing passages -----

    private fun readPassages(): List<String> {
        val raw = prefs.getString(KEY_PASSAGES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
        } catch (t: Throwable) {
            emptyList()
        }
    }

    fun addPassage(text: String) {
        val cleaned = text.trim()
        if (cleaned.length < 20) return
        val list = _customPassages.value + cleaned
        savePassages(list)
    }

    fun removePassage(text: String) = savePassages(_customPassages.value.filterNot { it == text })

    private fun savePassages(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString(KEY_PASSAGES, arr.toString()).apply()
        _customPassages.value = list
    }

    var useCustomPassages: Boolean
        get() = isReady && prefs.getBoolean(KEY_USE_CUSTOM, false)
        set(value) {
            if (isReady) prefs.edit().putBoolean(KEY_USE_CUSTOM, value).apply()
        }

    /** Remembers the last alarm shape you built, so the next one starts from it. */
    fun saveTemplate(alarm: Alarm) {
        if (!isReady) return
        prefs.edit().putString("template", AlarmJson.alarmToJson(alarm).toString()).apply()
    }

    fun template(): Alarm? {
        if (!isReady) return null
        val raw = prefs.getString("template", null) ?: return null
        return try {
            AlarmJson.alarmFromJson(JSONObject(raw))
        } catch (t: Throwable) {
            null
        }
    }
}
