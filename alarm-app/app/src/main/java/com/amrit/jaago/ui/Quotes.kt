package com.amrit.jaago.ui

import java.util.Calendar

data class Quote(val text: String, val by: String)

/**
 * One a day, picked by the day of the year so it is the same all day and
 * different tomorrow. Plain ASCII, short enough for a lock screen.
 */
object Quotes {

    const val MOTTO = "Arise, awake, and stop not till the goal is reached."
    const val MOTTO_BY = "Swami Vivekananda"

    private val ALL = listOf(
        Quote(MOTTO, MOTTO_BY),
        Quote("Take up one idea. Make that one idea your life; dream of it; think of it; live on that idea.", "Swami Vivekananda"),
        Quote("Strength is life, weakness is death.", "Swami Vivekananda"),
        Quote("Dream is not that which you see while sleeping; it is something that does not let you sleep.", "A. P. J. Abdul Kalam"),
        Quote("You have to dream before your dreams can come true.", "A. P. J. Abdul Kalam"),
        Quote("The best way to predict your future is to create it.", "Peter Drucker"),
        Quote("Discipline is choosing between what you want now and what you want most.", "Abraham Lincoln, attributed"),
        Quote("We are what we repeatedly do. Excellence, then, is not an act but a habit.", "Will Durant on Aristotle"),
        Quote("It always seems impossible until it is done.", "Nelson Mandela"),
        Quote("The secret of getting ahead is getting started.", "Mark Twain"),
        Quote("Well begun is half done.", "Aristotle"),
        Quote("Either you run the day or the day runs you.", "Jim Rohn"),
        Quote("Lose an hour in the morning, and you will be all day hunting for it.", "Richard Whately"),
        Quote("The future depends on what you do today.", "Mahatma Gandhi"),
        Quote("Small daily improvements are the key to staggering long term results.", "Robin Sharma"),
        Quote("Do not pray for an easy life, pray for the strength to endure a difficult one.", "Bruce Lee"),
        Quote("Success is the sum of small efforts, repeated day in and day out.", "Robert Collier"),
        Quote("What you do today can improve all your tomorrows.", "Ralph Marston"),
        Quote("Hard work beats talent when talent does not work hard.", "Tim Notke"),
        Quote("The early morning has gold in its mouth.", "Benjamin Franklin"),
        Quote("One hour of study in the morning is worth two in the evening.", "Old proverb"),
        Quote("Your future is created by what you do today, not tomorrow.", "Robert Kiyosaki"),
        Quote("Every morning we are born again. What we do today matters most.", "Buddhist saying"),
        Quote("Rise before the sun and the day belongs to you.", "Proverb")
    )

    fun ofDay(now: Long = System.currentTimeMillis()): Quote {
        val day = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_YEAR)
        return ALL[day % ALL.size]
    }

    /** A different one each time the alarm rings. */
    fun random(): Quote = ALL.random()

    fun greeting(now: Long = System.currentTimeMillis(), name: String = ""): String {
        val hour = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.HOUR_OF_DAY)
        val base = when (hour) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
        return if (name.isBlank()) base else "$base, $name"
    }
}
