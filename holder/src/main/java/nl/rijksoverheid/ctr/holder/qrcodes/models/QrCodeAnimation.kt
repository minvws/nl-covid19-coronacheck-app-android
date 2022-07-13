package nl.rijksoverheid.ctr.holder.qrcodes.models

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
sealed class QrCodeAnimation(@RawRes val animationResource: Int, @StringRes val contentDescription: Int) {
    object DomesticWinter : QrCodeAnimation(R.raw.winter_domestic, R.string.holder_showqr_animation_winterctb_voiceover_label)
    object EuWinter : QrCodeAnimation(R.raw.winter_international, R.string.holder_showqr_animation_winterdcc_voiceover_label)
    object DomesticSummer : QrCodeAnimation(R.raw.summer_domestic, R.string.holder_showqr_animation_summerctb_voiceover_label)
    object EuSummer : QrCodeAnimation(R.raw.summer_international, R.string.holder_showqr_animation_summerdcc_voiceover_label)
}