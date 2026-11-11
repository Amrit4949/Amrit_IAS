package com.amrit.jaago.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

/** The noise that gets made. All of them except SYSTEM are generated on the fly. */
enum class SoundProfile(
    val id: String,
    val title: String,
    val blurb: String
) {
    SCREECH("screech", "Dissonant screech", "Two clashing tones beating against each other. The nastiest one."),
    SIREN("siren", "Rising siren", "A square wave that climbs and drops. Air-raid energy."),
    STAB("stab", "Pulse stab", "Hard 3 kHz pulses, right where the human ear is most sensitive."),
    KLAXON("klaxon", "Klaxon", "Two-note industrial honk that will not blend into a dream."),
    AIRHORN("airhorn", "Air horn", "Fat, overdriven, stadium-sized blast."),
    SYSTEM("system", "Phone's own alarm tone", "Your normal ringtone. The merciful option.");

    companion object {
        fun from(id: String?): SoundProfile = values().firstOrNull { it.id == id } ?: SCREECH
    }
}

enum class MathDifficulty(val id: String, val title: String) {
    EASY("easy", "Easy"),
    MEDIUM("medium", "Medium"),
    HARD("hard", "Hard");

    companion object {
        fun from(id: String?): MathDifficulty = values().firstOrNull { it.id == id } ?: MEDIUM
    }
}

enum class TypingDifficulty(val id: String, val title: String) {
    HARD("hard", "Hard"),
    VERY_HARD("very_hard", "Very hard");

    companion object {
        fun from(id: String?): TypingDifficulty = values().firstOrNull { it.id == id } ?: HARD
    }
}

/** One hurdle between you and silence. They are run in order. */
sealed class ChallengeSpec {

    abstract val typeId: String

    /** Scan a physical barcode or QR code that you parked in another room. */
    data class Barcode(
        val value: String,
        val format: String,
        val label: String
    ) : ChallengeSpec() {
        override val typeId: String = TYPE
        companion object { const val TYPE = "barcode" }
    }

    data class Maths(
        val difficulty: MathDifficulty,
        val count: Int
    ) : ChallengeSpec() {
        override val typeId: String = TYPE
        companion object { const val TYPE = "maths" }
    }

    data class Typing(
        val difficulty: TypingDifficulty,
        val count: Int
    ) : ChallengeSpec() {
        override val typeId: String = TYPE
        companion object { const val TYPE = "typing" }
    }

    /** Two or three words for a mission slot tile. */
    fun shortLabel(): String = when (this) {
        is Barcode -> if (label.isBlank()) "Scan" else label.take(14)
        is Maths -> "$count x ${difficulty.title}"
        is Typing -> (if (count > 1) "$count x " else "") + difficulty.title
    }

    fun describe(): String = when (this) {
        is Barcode -> if (label.isBlank()) "Scan the saved code" else "Scan: $label"
        is Maths -> "$count ${difficulty.title.lowercase()} maths question" + (if (count == 1) "" else "s")
        is Typing -> "$count ${difficulty.title.lowercase()} passage" + (if (count == 1) "" else "s") + " to type"
    }
}

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val enabled: Boolean = true,
    /** java.util.Calendar day-of-week values (1 = Sunday .. 7 = Saturday). Empty means one-shot. */
    val days: Set<Int> = emptySet(),
    val sound: SoundProfile = SoundProfile.SCREECH,
    val vibrate: Boolean = true,
    /** Alarm stream volume while ringing, as a percentage of the phone's maximum. */
    val volumePercent: Int = 100,
    /** Drives the synthesised tone harder into clipping, which sounds louder. */
    val extraLoud: Boolean = true,
    /** Created from the Quick alarm sheet; deleted once it has rung. */
    val isQuick: Boolean = false,
    val snoozeMinutes: Int = 0,
    val maxSnoozes: Int = 2,
    val challenges: List<ChallengeSpec> = emptyList(),
    val autoSilenceMinutes: Int = 15,
    val strictTyping: Boolean = false,
    /** A scene id from Wallpapers, "auto", or "photo:<file>" for the user's own image. */
    val wallpaper: String = "auto",
    /** Seconds over which the sound climbs from quiet to full. 0 means straight in. */
    val rampSeconds: Int = 0
) {
    val snoozeEnabled: Boolean get() = snoozeMinutes > 0 && maxSnoozes > 0

    fun timeText(): String = String.format("%02d:%02d", hour, minute)

    /** "5:30" in 12 hour mode, "05:30" in 24 hour mode. */
    fun clockMain(is24: Boolean): String =
        if (is24) String.format("%02d:%02d", hour, minute)
        else String.format("%d:%02d", if (hour % 12 == 0) 12 else hour % 12, minute)

    /** "am" / "pm", or empty in 24 hour mode. */
    fun clockSuffix(is24: Boolean): String = if (is24) "" else if (hour < 12) "am" else "pm"

    fun clockFull(is24: Boolean): String =
        clockMain(is24) + clockSuffix(is24).let { if (it.isEmpty()) "" else " $it" }

    fun daysText(): String {
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        if (days == WEEKDAYS) return "Mon to Fri"
        if (days == WEEKEND) return "Weekends"
        return DAY_ORDER.filter { days.contains(it) }.joinToString(" ") { shortDayName(it) }
    }

    companion object {
        const val MAX_MISSIONS = 5

        /** Sunday first, the way the week is laid out on screen. */
        val DISPLAY_ORDER = listOf(
            Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
        )

        val DAY_ORDER = listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        )
        val WEEKDAYS = setOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
        )
        val WEEKEND = setOf(Calendar.SATURDAY, Calendar.SUNDAY)

        fun letterDayName(day: Int): String = when (day) {
            Calendar.MONDAY -> "M"
            Calendar.TUESDAY -> "T"
            Calendar.WEDNESDAY -> "W"
            Calendar.THURSDAY -> "T"
            Calendar.FRIDAY -> "F"
            Calendar.SATURDAY -> "S"
            else -> "S"
        }

        fun shortDayName(day: Int): String = when (day) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> "Sun"
        }
    }
}

/** A line in the wake-up log, so you can see how long it actually takes you to get up. */
data class WakeEvent(
    val atMillis: Long,
    val label: String,
    val outcome: String,
    val secondsTaken: Int
)

// ---------------------------------------------------------------------------
// JSON (hand written on purpose: no annotation processor, nothing to go stale)
// ---------------------------------------------------------------------------

object AlarmJson {

    fun alarmToJson(a: Alarm): JSONObject {
        val o = JSONObject()
        o.put("id", a.id)
        o.put("hour", a.hour)
        o.put("minute", a.minute)
        o.put("label", a.label)
        o.put("enabled", a.enabled)
        o.put("days", JSONArray().also { arr -> a.days.sorted().forEach { arr.put(it) } })
        o.put("sound", a.sound.id)
        o.put("vibrate", a.vibrate)
        o.put("volumePercent", a.volumePercent)
        o.put("extraLoud", a.extraLoud)
        o.put("isQuick", a.isQuick)
        o.put("snoozeMinutes", a.snoozeMinutes)
        o.put("maxSnoozes", a.maxSnoozes)
        o.put("autoSilenceMinutes", a.autoSilenceMinutes)
        o.put("strictTyping", a.strictTyping)
        o.put("wallpaper", a.wallpaper)
        o.put("rampSeconds", a.rampSeconds)
        val ch = JSONArray()
        a.challenges.forEach { ch.put(challengeToJson(it)) }
        o.put("challenges", ch)
        return o
    }

    fun alarmFromJson(o: JSONObject): Alarm {
        val days = mutableSetOf<Int>()
        o.optJSONArray("days")?.let { arr ->
            for (i in 0 until arr.length()) days.add(arr.optInt(i))
        }
        val challenges = mutableListOf<ChallengeSpec>()
        o.optJSONArray("challenges")?.let { arr ->
            for (i in 0 until arr.length()) {
                arr.optJSONObject(i)?.let { c -> challengeFromJson(c)?.let { challenges.add(it) } }
            }
        }
        return Alarm(
            id = o.optInt("id"),
            hour = o.optInt("hour").coerceIn(0, 23),
            minute = o.optInt("minute").coerceIn(0, 59),
            label = o.optString("label", ""),
            enabled = o.optBoolean("enabled", true),
            days = days,
            sound = SoundProfile.from(o.optString("sound")),
            vibrate = o.optBoolean("vibrate", true),
            volumePercent = o.optInt("volumePercent", 100).coerceIn(10, 100),
            extraLoud = o.optBoolean("extraLoud", true),
            isQuick = o.optBoolean("isQuick", false),
            snoozeMinutes = o.optInt("snoozeMinutes", 0),
            maxSnoozes = o.optInt("maxSnoozes", 2),
            challenges = challenges,
            autoSilenceMinutes = o.optInt("autoSilenceMinutes", 15),
            strictTyping = o.optBoolean("strictTyping", false),
            wallpaper = o.optString("wallpaper", "auto").ifBlank { "auto" },
            rampSeconds = o.optInt("rampSeconds", 0).coerceIn(0, 300)
        )
    }

    private fun challengeToJson(c: ChallengeSpec): JSONObject {
        val o = JSONObject()
        o.put("type", c.typeId)
        when (c) {
            is ChallengeSpec.Barcode -> {
                o.put("value", c.value)
                o.put("format", c.format)
                o.put("label", c.label)
            }
            is ChallengeSpec.Maths -> {
                o.put("difficulty", c.difficulty.id)
                o.put("count", c.count)
            }
            is ChallengeSpec.Typing -> {
                o.put("difficulty", c.difficulty.id)
                o.put("count", c.count)
            }
        }
        return o
    }

    private fun challengeFromJson(o: JSONObject): ChallengeSpec? = when (o.optString("type")) {
        ChallengeSpec.Barcode.TYPE -> ChallengeSpec.Barcode(
            value = o.optString("value", ""),
            format = o.optString("format", ""),
            label = o.optString("label", "")
        ).takeIf { it.value.isNotBlank() }
        ChallengeSpec.Maths.TYPE -> ChallengeSpec.Maths(
            difficulty = MathDifficulty.from(o.optString("difficulty")),
            count = o.optInt("count", 3).coerceIn(1, 20)
        )
        ChallengeSpec.Typing.TYPE -> ChallengeSpec.Typing(
            difficulty = TypingDifficulty.from(o.optString("difficulty")),
            count = o.optInt("count", 1).coerceIn(1, 10)
        )
        else -> null
    }

    fun eventToJson(e: WakeEvent): JSONObject = JSONObject()
        .put("at", e.atMillis)
        .put("label", e.label)
        .put("outcome", e.outcome)
        .put("seconds", e.secondsTaken)

    fun eventFromJson(o: JSONObject): WakeEvent = WakeEvent(
        atMillis = o.optLong("at"),
        label = o.optString("label", ""),
        outcome = o.optString("outcome", ""),
        secondsTaken = o.optInt("seconds", 0)
    )
}
