package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import org.json.JSONObject

sealed class QrCodeData(
    open val bitmap: Bitmap,
    @RawRes open val animationResource: Int,
    @DrawableRes open val backgroundResource: Int?
) {

    data class Domestic(
        override val bitmap: Bitmap,
        override val animationResource: Int,
        override val backgroundResource: Int?,
        val readDomesticCredential: ReadDomesticCredential
    ) : QrCodeData(bitmap, animationResource, backgroundResource)

    data class European(
        override val bitmap: Bitmap,
        override val animationResource: Int,
        override val backgroundResource: Int?,
        val readEuropeanCredential: JSONObject
    ) : QrCodeData(bitmap, animationResource, backgroundResource)
}