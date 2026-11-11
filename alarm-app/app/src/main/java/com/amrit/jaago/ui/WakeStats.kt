package com.amrit.jaago.ui

import com.amrit.jaago.model.WakeEvent
import java.util.Calendar

/** Numbers derived from the wake-up log. */
object WakeStats {

    private fun dayKey(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    private fun previousDay(key: Int): Int {
        val c = Calendar.getInstance().apply {
            set(Calendar.YEAR, key / 1000)
            set(Calendar.DAY_OF_YEAR, key % 1000)
            add(Calendar.DAY_OF_YEAR, -1)
        }
        return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Consecutive days, counting back from today or yesterday, on which at least
     * one alarm was beaten. A missed alarm on a day breaks it.
     */
    fun streak(history: List<WakeEvent>, now: Long = System.currentTimeMillis()): Int {
        if (history.isEmpty()) return 0
        val byDay = HashMap<Int, MutableList<WakeEvent>>()
        for (e in history) byDay.getOrPut(dayKey(e.atMillis)) { mutableListOf() }.add(e)
        fun beaten(day: Int): Boolean {
            val events = byDay[day] ?: return false
            return events.any { it.outcome.startsWith("Woke") } && events.none { it.outcome.startsWith("Missed") }
        }
        var day = dayKey(now)
        // Today may not have had its alarm yet; start from yesterday in that case.
        if (!beaten(day)) day = previousDay(day)
        var count = 0
        while (beaten(day)) {
            count++
            day = previousDay(day)
        }
        return count
    }

    fun bestStreak(history: List<WakeEvent>): Int {
        if (history.isEmpty()) return 0
        val days = history
            .filter { it.outcome.startsWith("Woke") }
            .map { dayKey(it.atMillis) }
            .toSortedSet()
        val missed = history.filter { it.outcome.startsWith("Missed") }.map { dayKey(it.atMillis) }.toSet()
        var best = 0
        var run = 0
        var prev = -1
        for (d in days) {
            if (d in missed) {
                run = 0
                prev = -1
                continue
            }
            run = if (prev != -1 && previousDay(d) == prev) run + 1 else 1
            best = maxOf(best, run)
            prev = d
        }
        return best
    }

    /** "Sleep by 10:30 pm for 7 h" for an alarm at [alarmAt]. */
    fun sleepByMillis(alarmAt: Long, hours: Int = 7): Long = alarmAt - hours * 3_600_000L
}
