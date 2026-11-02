package com.amrit.jaago.challenge

import com.amrit.jaago.model.MathDifficulty
import kotlin.random.Random

/**
 * Every question has a single whole number answer, so the keypad can stay numeric
 * and there is never an argument about rounding.
 *
 * Medium and hard lean on the arithmetic that actually turns up in competitive
 * exam papers: percentages, averages, ratios, simple interest, series.
 */
object MathChallenge {

    data class Question(val prompt: String, val answer: Int)

    fun generate(difficulty: MathDifficulty, random: Random = Random.Default): Question =
        when (difficulty) {
            MathDifficulty.EASY -> easy(random)
            MathDifficulty.MEDIUM -> medium(random)
            MathDifficulty.HARD -> hard(random)
        }

    // --- easy: you can do these with one eye open ------------------------------

    private fun easy(r: Random): Question = when (r.nextInt(4)) {
        0 -> {
            val a = r.nextInt(11, 90)
            val b = r.nextInt(11, 90)
            Question("$a + $b", a + b)
        }
        1 -> {
            val a = r.nextInt(35, 100)
            val b = r.nextInt(11, a - 5)
            Question("$a - $b", a - b)
        }
        2 -> {
            val a = r.nextInt(3, 10)
            val b = r.nextInt(3, 13)
            Question("$a x $b", a * b)
        }
        else -> {
            val a = r.nextInt(10, 45)
            val b = r.nextInt(10, 45)
            val c = r.nextInt(10, 45)
            Question("$a + $b + $c", a + b + c)
        }
    }

    // --- medium ---------------------------------------------------------------

    private fun medium(r: Random): Question = when (r.nextInt(6)) {
        0 -> {
            val a = r.nextInt(12, 30)
            val b = r.nextInt(3, 10)
            Question("$a x $b", a * b)
        }
        1 -> {
            val a = r.nextInt(120, 900)
            val b = r.nextInt(120, 900)
            Question("$a + $b", a + b)
        }
        2 -> {
            val q = r.nextInt(7, 20)
            val b = r.nextInt(3, 13)
            Question("${q * b} / $b", q)
        }
        3 -> {
            val percent = listOf(5, 10, 15, 20, 25, 40, 50, 60, 75).random(r)
            val base = r.nextInt(6, 46) * 20
            Question("$percent% of $base", base * percent / 100)
        }
        4 -> {
            val a = r.nextInt(15, 60)
            val b = r.nextInt(4, 13)
            val c = r.nextInt(3, 10)
            Question("$a + $b x $c", a + b * c)
        }
        else -> {
            val mean = r.nextInt(20, 61)
            val d1 = r.nextInt(-9, 10)
            val d2 = r.nextInt(-9, 10)
            val d3 = r.nextInt(-9, 10)
            val n1 = mean + d1
            val n2 = mean + d2
            val n3 = mean + d3
            val n4 = 4 * mean - n1 - n2 - n3
            Question("Average of $n1, $n2, $n3 and $n4", mean)
        }
    }

    // --- hard -----------------------------------------------------------------

    private fun hard(r: Random): Question = when (r.nextInt(9)) {
        0 -> {
            val a = r.nextInt(12, 50)
            val b = r.nextInt(11, 40)
            Question("$a x $b", a * b)
        }
        1 -> {
            val a = r.nextInt(12, 40)
            val b = r.nextInt(8, 35)
            val c = r.nextInt(3, 10)
            val d = r.nextInt(20, 150)
            Question("($a + $b) x $c - $d", (a + b) * c - d)
        }
        2 -> {
            val n = r.nextInt(13, 40)
            Question("$n squared", n * n)
        }
        3 -> {
            // Percentage rise, always a whole number.
            val base = r.nextInt(2, 13) * 50
            val percent = listOf(10, 20, 25, 30, 40, 50, 60).random(r)
            val after = base + base * percent / 100
            Question("A value rises from $base to $after. The rise is what per cent?", percent)
        }
        4 -> {
            val principal = r.nextInt(2, 21) * 1000
            val rate = r.nextInt(2, 13)
            val years = r.nextInt(2, 6)
            Question(
                "Simple interest on Rs $principal at $rate% a year for $years years",
                principal * rate * years / 100
            )
        }
        5 -> {
            val r1 = r.nextInt(2, 8)
            val r2 = r.nextInt(2, 8) + r1
            val unit = r.nextInt(6, 40)
            val total = (r1 + r2) * unit
            Question("Divide $total in the ratio $r1 : $r2. The bigger share is", r2 * unit)
        }
        6 -> {
            val first = r.nextInt(3, 12)
            val step = r.nextInt(4, 15)
            val terms = (0..4).map { first + it * step }
            Question("Next term: ${terms.joinToString(", ")}, ?", first + 5 * step)
        }
        7 -> {
            val a = r.nextInt(12, 40)
            val b = r.nextInt(5, 20)
            val c = r.nextInt(6, 25)
            val d = r.nextInt(4, 15)
            Question("$a x $b - $c x $d", a * b - c * d)
        }
        else -> {
            val p1 = listOf(10, 20, 25, 40, 50).random(r)
            val p2 = listOf(10, 20, 25, 40, 50).random(r)
            val n1 = r.nextInt(3, 21) * 20
            val n2 = r.nextInt(3, 21) * 20
            Question("$p1% of $n1 + $p2% of $n2", n1 * p1 / 100 + n2 * p2 / 100)
        }
    }

    /** A short line of text explaining what the tier involves, for the editor screen. */
    fun describe(difficulty: MathDifficulty): String = when (difficulty) {
        MathDifficulty.EASY -> "Two digit sums, small multiplication"
        MathDifficulty.MEDIUM -> "Three digit sums, percentages, averages, exact division"
        MathDifficulty.HARD -> "Two digit multiplication, ratios, simple interest, series, per cent change"
    }
}
