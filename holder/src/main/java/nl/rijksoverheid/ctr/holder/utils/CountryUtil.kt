/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.utils

import java.util.Locale

interface CountryUtil {
    fun getCountryForInfoScreen(
        deviceLanguage: String,
        countryCode: String
    ): String

    fun getCountryForQrInfoScreen(
        countryCode: String?,
        currentLocale: Locale?
    ): String
}

class CountryUtilImpl : CountryUtil {
    override fun getCountryForInfoScreen(deviceLanguage: String, countryCode: String): String {
        val countriesMap: MutableMap<String, String> = mutableMapOf()
        Locale.getISOCountries().forEach {
            val locale = Locale(deviceLanguage, it)
            val countryIso3Code = locale.isO3Country
            val fullCountryName = locale.displayCountry
            countriesMap[countryIso3Code] = fullCountryName
            countriesMap[it] = fullCountryName
        }

        if (countriesMap[countryCode] == "Netherlands") {
            return "The Netherlands"
        }

        return countriesMap[countryCode] ?: countryCode
    }

    override fun getCountryForQrInfoScreen(countryCode: String?, currentLocale: Locale?): String = if (countryCode != null) {
        val localeIsNL = currentLocale?.language == "nl"
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
