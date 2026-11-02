package com.amrit.jaago.challenge

import com.amrit.jaago.model.AlarmStore
import com.amrit.jaago.model.TypingDifficulty
import kotlin.random.Random

/**
 * Typing passages. Deliberately plain ASCII: no curly quotes, no dashes that need
 * a long press, nothing you cannot reach on a stock phone keyboard at five in the
 * morning.
 *
 * The passages are revision material rather than filler, so the punishment is at
 * least useful. You can add your own in Settings.
 */
object TypingChallenge {

    private val HARD = listOf(
        "Article 14 of the Constitution guarantees equality before the law and the equal protection of the laws to all persons within the territory of India.",
        "The Directive Principles of State Policy are contained in Part IV of the Constitution and are not enforceable by any court of law.",
        "The Finance Commission is a constitutional body under Article 280, constituted every fifth year to recommend how tax revenue is shared.",
        "A money bill can be introduced only in the Lok Sabha, and only on the recommendation of the President of India.",
        "The Fundamental Duties were added by the Forty Second Amendment Act of 1976, on the recommendation of the Swaran Singh Committee.",
        "The Election Commission of India is a permanent constitutional body established under Article 324 to conduct free and fair elections.",
        "The repo rate is the rate at which the Reserve Bank of India lends short term funds to commercial banks against government securities.",
        "The Western Ghats, a global biodiversity hotspot, run parallel to the western coast of India for about sixteen hundred kilometres.",
        "The Preamble declares India to be a sovereign, socialist, secular, democratic republic committed to justice, liberty, equality and fraternity.",
        "The Comptroller and Auditor General is appointed under Article 148 and audits all receipts and expenditure of the Union and the States.",
        "Retail inflation measured by the Consumer Price Index tracks the change in prices of a fixed basket of goods and services over time.",
        "The Tropic of Cancer passes through eight Indian states, among them Gujarat, Rajasthan, Madhya Pradesh, Jharkhand and West Bengal.",
        "Writs of habeas corpus, mandamus, prohibition, certiorari and quo warranto can be issued by the Supreme Court under Article 32.",
        "The National Green Tribunal was set up in 2010 to dispose of cases relating to environmental protection and the conservation of forests."
    )

    private val VERY_HARD = listOf(
        "The Constitution of India establishes a quasi federal polity in which the Union and the States draw their powers from the same instrument; the Seventh Schedule splits legislative subjects into the Union List, the State List and the Concurrent List, while residuary powers rest with Parliament under Article 248.",
        "Judicial review flows from Articles 13, 32 and 226 rather than from any single clause; in Kesavananda Bharati versus State of Kerala, decided in 1973, the Supreme Court held that Parliament may amend any part of the Constitution but cannot damage its basic structure.",
        "Monetary policy is framed by a six member Monetary Policy Committee chaired by the Governor of the Reserve Bank of India, which is mandated to hold retail inflation at four per cent, with a tolerance band of two percentage points on either side of that target.",
        "The Green Revolution of the middle nineteen sixties combined high yielding varieties of wheat and rice with assured irrigation, chemical fertiliser and guaranteed procurement prices; it ended recurrent famine, but it also widened regional disparity and drew down the groundwater of Punjab and Haryana.",
        "Under the Seventy Third Amendment Act of 1992 a three tier structure of village, intermediate and district panchayats became mandatory for every state with a population above twenty lakh, with one third of all seats and of the offices of chairperson reserved for women.",
        "The south west monsoon, which reaches the Kerala coast in the first week of June, is driven by the seasonal reversal of pressure over the Tibetan plateau and by the northward march of the intertropical convergence zone, and it delivers close to three quarters of the annual rainfall of the country.",
        "The Government of India Act of 1935 introduced provincial autonomy, ended dyarchy in the provinces and provided for a federation of British India and the princely states; the federal part never came into force, yet much of its administrative machinery was carried into the Constitution that took effect in 1950.",
        "Fiscal consolidation under the Fiscal Responsibility and Budget Management Act requires the Union government to cap its fiscal deficit, disclose contingent liabilities and publish a medium term expenditure framework, so that borrowing pays for capital formation rather than for routine revenue spending.",
        "Soil erosion in the Chambal badlands, salinity in the canal commands of Punjab and water logging in the Indo Gangetic plain are together a reminder that land degradation is not a single problem but a set of local failures of drainage, cropping choice and common property management."
    )

    /** Picks a passage. Your own passages win when you have switched them on. */
    fun pick(difficulty: TypingDifficulty, random: Random = Random.Default): String {
        val custom = if (AlarmStore.isReady && AlarmStore.useCustomPassages) {
            AlarmStore.customPassages.value
        } else {
            emptyList()
        }
        val pool = when {
            custom.isNotEmpty() -> custom
            difficulty == TypingDifficulty.VERY_HARD -> VERY_HARD
            else -> HARD
        }
        return pool[random.nextInt(pool.size)]
    }

    fun sample(difficulty: TypingDifficulty): String =
        if (difficulty == TypingDifficulty.VERY_HARD) VERY_HARD.first() else HARD.first()

    fun poolSize(difficulty: TypingDifficulty): Int =
        if (difficulty == TypingDifficulty.VERY_HARD) VERY_HARD.size else HARD.size

    fun describe(difficulty: TypingDifficulty): String = when (difficulty) {
        TypingDifficulty.HARD -> "One long sentence, around 140 characters"
        TypingDifficulty.VERY_HARD -> "A dense passage of 250 to 330 characters, with commas and semicolons"
    }

    // --- checking -------------------------------------------------------------

    private val whitespace = Regex("\\s+")

    fun normalise(text: String, strictCase: Boolean): String {
        val collapsed = text.trim().replace(whitespace, " ")
        return if (strictCase) collapsed else collapsed.lowercase()
    }

    fun isComplete(typed: String, target: String, strictCase: Boolean): Boolean =
        normalise(typed, strictCase) == normalise(target, strictCase)

    /** Character level check used to colour the text as it is typed. */
    fun charMatches(typed: Char, target: Char, strictCase: Boolean): Boolean =
        if (strictCase) typed == target else typed.lowercaseChar() == target.lowercaseChar()

    /** How many leading characters are still correct. Drives the progress bar. */
    fun correctPrefixLength(typed: String, target: String, strictCase: Boolean): Int {
        var i = 0
        while (i < typed.length && i < target.length && charMatches(typed[i], target[i], strictCase)) i++
        return i
    }

    fun mistakeCount(typed: String, target: String, strictCase: Boolean): Int {
        var wrong = 0
        for (i in typed.indices) {
            val t = target.getOrNull(i) ?: break
            if (!charMatches(typed[i], t, strictCase)) wrong++
        }
        return wrong
    }
}
