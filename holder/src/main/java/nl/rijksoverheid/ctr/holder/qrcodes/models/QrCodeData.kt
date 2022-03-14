package nl.rijksoverheid.ctr.holder.qrcodes.models

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import org.json.JSONObject

sealed class QrCodeData(
    open val bitmap: Bitmap,
) {

    data class Domestic(
        override val bitmap: Bitmap,
        val readDomesticCredential: ReadDomesticCredential
    ) : QrCodeData(bitmap)

    sealed class European(
        override val bitmap: Bitmap,
        open val readEuropeanCredential: JSONObject
    ) : QrCodeData(bitmap) {

        data class Vaccination(
            val dose: String,
            val ofTotalDoses: String,
            val isHidden: Boolean,
            override val bitmap: Bitmap,
            override val readEuropeanCredential: JSONObject
        ): European(bitmap, readEuropeanCredential)

        data class NonVaccination(
            override val bitmap: Bitmap,
            override val readEuropeanCredential: JSONObject
        ) : European(bitmap, readEuropeanCredential)
    }
}