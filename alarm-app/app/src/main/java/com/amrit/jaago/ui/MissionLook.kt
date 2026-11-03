package com.amrit.jaago.ui

import androidx.compose.ui.graphics.Color
import com.amrit.jaago.R
import com.amrit.jaago.model.ChallengeSpec
import com.amrit.jaago.ui.theme.Maths1
import com.amrit.jaago.ui.theme.Maths2
import com.amrit.jaago.ui.theme.Scan1
import com.amrit.jaago.ui.theme.Scan2
import com.amrit.jaago.ui.theme.Type1
import com.amrit.jaago.ui.theme.Type2

/** Icon and primary accent for a mission. */
internal fun challengeLook(spec: ChallengeSpec): Pair<Int, Color> = when (spec) {
    is ChallengeSpec.Barcode -> R.drawable.ic_qr to Scan1
    is ChallengeSpec.Maths -> R.drawable.ic_calc to Maths1
    is ChallengeSpec.Typing -> R.drawable.ic_keyboard to Type1
}

/** Both ends of a mission's gradient. */
internal fun challengeGradient(spec: ChallengeSpec): Pair<Color, Color> = when (spec) {
    is ChallengeSpec.Barcode -> Scan1 to Scan2
    is ChallengeSpec.Maths -> Maths1 to Maths2
    is ChallengeSpec.Typing -> Type1 to Type2
}

internal fun challengeTitle(spec: ChallengeSpec): String = when (spec) {
    is ChallengeSpec.Barcode -> "Barcode"
    is ChallengeSpec.Maths -> "Maths"
    is ChallengeSpec.Typing -> "Typing"
}
