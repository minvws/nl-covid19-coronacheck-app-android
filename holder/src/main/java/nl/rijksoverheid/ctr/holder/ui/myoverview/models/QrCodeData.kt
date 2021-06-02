package nl.rijksoverheid.ctr.holder.ui.myoverview.models

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import org.json.JSONObject

sealed class QrCodeData(open val bitmap: Bitmap) {

    data class Domestic(override val bitmap: Bitmap, val readDomesticCredential: ReadDomesticCredential) : QrCodeData(bitmap)
    data class European(override val bitmap: Bitmap, val readEuropeanCredential: JSONObject) : QrCodeData(bitmap)
}