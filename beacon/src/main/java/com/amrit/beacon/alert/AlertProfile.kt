package com.amrit.beacon.alert

/**
 * How hard to push, chosen by whoever taps "ring" and carried in the signed message so it
 * cannot be escalated in flight.
 *
 * Having three levels rather than one button matters: most of the time you have simply
 * misplaced the phone in the same room, and blasting a siren with the torch strobing is
 * obnoxious. Reserve that for the case where it is genuinely lost or someone needs to be
 * woken up.
 */
enum class AlertProfile(
    val wireName: String,
    val label: String,
    val description: String,
    /**
     * Extra gain, in millibels, applied by `LoudnessEnhancer` on top of the device maximum.
     * Above roughly 2000 the output saturates audibly on most phone speakers — which is
     * effective for cutting through noise, but is not something to do by default.
     */
    val boostMillibels: Int,
    val overrideVolume: Boolean,
    val overrideDnd: Boolean,
    val vibrate: Boolean,
    val strobeTorch: Boolean,
) {
    GENTLE(
        wireName = "gentle",
        label = "Gentle",
        description = "Plays at the volume already set. Does not touch silent mode.",
        boostMillibels = 0,
        overrideVolume = false,
        overrideDnd = false,
        vibrate = true,
        strobeTorch = false,
    ),
    LOUD(
        wireName = "loud",
        label = "Loud",
        description = "Full alarm volume through silent mode and Do Not Disturb.",
        boostMillibels = 0,
        overrideVolume = true,
        overrideDnd = true,
        vibrate = true,
        strobeTorch = false,
    ),
    MAX(
        wireName = "max",
        label = "Maximum",
        description = "Everything Loud does, plus extra gain and a strobing torch.",
        boostMillibels = 1800,
        overrideVolume = true,
        overrideDnd = true,
        vibrate = true,
        strobeTorch = true,
    );

    companion object {
        val DEFAULT = LOUD

        fun fromWireName(name: String): AlertProfile =
            entries.firstOrNull { it.wireName == name } ?: DEFAULT
    }
}
