package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

data class QrCodeViewData(
    val qrCodeData: QrCodeData,
    @RawRes val animationResource: Int,
    @DrawableRes val backgroundResource: Int?
)