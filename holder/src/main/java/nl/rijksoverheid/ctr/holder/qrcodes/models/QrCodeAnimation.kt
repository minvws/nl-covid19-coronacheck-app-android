package nl.rijksoverheid.ctr.holder.qrcodes.models

import androidx.annotation.RawRes
import nl.rijksoverheid.ctr.holder.R

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
sealed class QrCodeAnimation(@RawRes val animationResource: Int) {
    object DomesticWinter : QrCodeAnimation(R.raw.winter_domestic)
    object EuWinter : QrCodeAnimation(R.raw.winter_international)
    object DomesticSummer : QrCodeAnimation(R.raw.summer_domestic)
    object EuSummer : QrCodeAnimation(R.raw.summer_international)
}