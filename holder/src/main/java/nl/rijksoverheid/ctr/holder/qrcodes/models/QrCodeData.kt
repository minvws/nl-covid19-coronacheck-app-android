package nl.rijksoverheid.ctr.holder.qrcodes.models

import android.graphics.Bitmap
import org.json.JSONObject

sealed class QrCodeData(
    open val bitmap: Bitmap
) {

    sealed class European(
        override val bitmap: Bitmap,
        open val isExpired: Boolean,
        open val readEuropeanCredential: JSONObject
    ) : QrCodeData(bitmap) {

        data class Vaccination(
            val dose: String,
            val ofTotalDoses: String,
            val isDoseNumberSmallerThanTotalDose: Boolean,
            override val isExpired: Boolean,
            override val bitmap: Bitmap,
            override val readEuropeanCredential: JSONObject
        ) : European(bitmap, isExpired, readEuropeanCredential)

        data class NonVaccination(
            override val isExpired: Boolean,
            override val bitmap: Bitmap,
            override val readEuropeanCredential: JSONObject
        ) : European(bitmap, isExpired, readEuropeanCredential)
    }
}
