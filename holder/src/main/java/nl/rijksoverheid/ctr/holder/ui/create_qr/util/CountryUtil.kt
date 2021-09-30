package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import java.util.*

interface CountryUtil {
    fun getCountry(
        countryCode: String?,
        currentLocale: Locale?
    ): String
}

class CountryUtilImpl: CountryUtil {
    override fun getCountry(countryCode: String?, currentLocale: Locale?): String = if (countryCode != null) {
        val localeIsNL = currentLocale?.country == "NL"
        val countryIsNL = countryCode == "NL"
        val countryNameInDutch = Locale("", countryCode).getDisplayCountry(Locale("nl"))
        val countryNameInEnglish = Locale("", countryCode).getDisplayCountry(Locale("en"))

        // GetDisplayCountry returns country for "NL" as "Netherlands" instead of "The Netherlands"
        if (localeIsNL && countryIsNL) {
            "$countryNameInDutch / The $countryNameInEnglish"
        } else if (localeIsNL) {
            "$countryNameInDutch / $countryNameInEnglish"
        } else {
            countryNameInEnglish
        }
    } else {
        ""
    }
}