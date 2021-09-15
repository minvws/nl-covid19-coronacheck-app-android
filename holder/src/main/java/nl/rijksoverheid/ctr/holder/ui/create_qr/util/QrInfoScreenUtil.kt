/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearNumerical
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

interface QrInfoScreenUtil {

    fun getForDomesticQr(personalDetails: PersonalDetails): InfoScreen

    fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): InfoScreen

    fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): InfoScreen

    fun getForEuropeanRecoveryQr(readEuropeanCredential: JSONObject): InfoScreen
}

class QrInfoScreenUtilImpl(
    private val resources: Resources,
    cachedAppConfigUseCase: CachedAppConfigUseCase
) : QrInfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForDomesticQr(personalDetails: PersonalDetails): InfoScreen {
        val title = resources.getString(R.string.qr_explanation_title_domestic)
        val description = resources.getString(
            R.string.qr_explanation_description_domestic,
            "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): InfoScreen {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val test = dcc.getJSONArray("t").optJSONObject(0)

        val title = resources.getString(R.string.qr_explanation_title_eu)

        val fullName = "${dcc.optJSONObject("nam").getStringOrNull("fn")}, ${
            dcc.optJSONObject("nam").getStringOrNull("gn")
        }"

        val birthDate = dcc.getStringOrNull("dob")?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val disease = resources.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == test.getStringOrNull("tt")
        }?.name ?: test.getStringOrNull("tt") ?: ""

        val testName = test.getStringOrNull("nm") ?: ""

        val testDate = test.getStringOrNull("sc")?.let {
            try {
                OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .toLocalDate().formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val testResult =
            resources.getString(R.string.your_test_result_explanation_negative_test_result)

        val testLocation = test.getStringOrNull("tc") ?: ""

        val manufacturer =
            holderConfig.euManufacturers.firstOrNull {
                it.code == test.getStringOrNull("ma")
            }?.name ?: test.getStringOrNull("ma") ?: ""

        val testCountry = getCountry(test.getStringOrNull("co"), getCurrentLocale())

        val issuerValue = test.getStringOrNull("is")
        val issuer = if (issuerValue == issuerVWS) {
            resources.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val uniqueCode = test.getStringOrNull("ci")

        return InfoScreen(
            title = title,
            description = (TextUtils.concat(
                resources.getString(R.string.qr_explanation_description_eu_test_header),
                "<br/><br/>",
                resources.getString(R.string.qr_explanation_description_eu_test_name),
                createQrAnswer(fullName),
                resources.getString(R.string.qr_explanation_description_eu_test_birth_date),
                createQrAnswer(birthDate),
                resources.getString(R.string.qr_explanation_description_eu_test_disease),
                createQrAnswer(disease),
                resources.getString(R.string.qr_explanation_description_eu_test_test_type),
                createQrAnswer(testType),
                resources.getString(R.string.qr_explanation_description_eu_test_test_name),
                createQrAnswer(testName),
                resources.getString(R.string.qr_explanation_description_eu_test_test_date),
                createQrAnswer(testDate),
                resources.getString(R.string.qr_explanation_description_eu_test_test_result),
                createQrAnswer(testResult),
                resources.getString(R.string.qr_explanation_description_eu_test_test_centre),
                createQrAnswer(testLocation),
                resources.getString(R.string.qr_explanation_description_eu_test_manufacturer),
                createQrAnswer(manufacturer),
                resources.getString(R.string.qr_explanation_description_eu_test_test_country),
                createQrAnswer(testCountry),
                resources.getString(R.string.qr_explanation_description_eu_test_issuer),
                createQrAnswer(issuer ?: ""),
                resources.getString(R.string.qr_explanation_description_eu_test_certificate_identifier),
                createQrAnswer(uniqueCode ?: ""),
                resources.getString(R.string.qr_explanation_description_eu_test_footer),
            ) as String)
        )
    }

    private fun getCurrentLocale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales[0]
    } else {
        resources.configuration.locale
    }

    private fun getCountry(
        countryCode: String?,
        currentLocale: Locale?
    ): String = if (countryCode != null) {
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

    override fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): InfoScreen {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val vaccination = dcc.getJSONArray("v").optJSONObject(0)

        val title = resources.getString(R.string.qr_explanation_title_eu)

        val fullName = "${dcc.optJSONObject("nam").getStringOrNull("fn")}, ${
            dcc.optJSONObject("nam").getStringOrNull("gn")
        }"

        val birthDate = dcc.getStringOrNull("dob")?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: DateTimeParseException) {
                // Check if date has removed content, if so return year or string only
                if (birthDate.contains("XX")) {
                    // Retrieve birth year only
                    birthDate.split("-").first()
                } else birthDate
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val disease = resources.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val vaccin = holderConfig.euBrands.firstOrNull {
            it.code == vaccination.getStringOrNull("mp")
        }?.name ?: vaccination.getStringOrNull("mp") ?: ""

        val vaccinType = holderConfig.euVaccinations.firstOrNull {
            it.code == vaccination.getStringOrNull("vp")
        }?.name ?: vaccination.getStringOrNull("vp") ?: ""

        val manufacturer =
            holderConfig.euManufacturers.firstOrNull {
                it.code == vaccination.getStringOrNull("ma")
            }?.name ?: vaccination.getStringOrNull("ma") ?: ""

        val doses =
            if (vaccination.getStringOrNull("dn") != null && vaccination.getStringOrNull("sd") != null) {
                resources.getString(
                    R.string.your_vaccination_explanation_doses_answer,
                    vaccination.getStringOrNull("dn"),
                    vaccination.getStringOrNull("sd")
                )
            } else ""

        val vaccinationDate = vaccination.getStringOrNull("dt")?.let { vaccinationDate ->
            try {
                LocalDate.parse(vaccinationDate, DateTimeFormatter.ISO_DATE)
                    .formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val countryCode = vaccination.getStringOrNull("co")
        val vaccinationCountry = getCountry(countryCode, getCurrentLocale())

        val issuerValue = vaccination.getStringOrNull("is")
        val issuer = if (issuerValue == issuerVWS) {
            resources.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val uniqueCode = vaccination.getStringOrNull("ci")

        return InfoScreen(
            title = title,
            description = (TextUtils.concat(
                resources.getString(R.string.qr_explanation_description_eu_vaccination_header),
                "<br/><br/>",
                resources.getString(R.string.qr_explanation_description_eu_vaccination_name),
                createQrAnswer(fullName),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_birth_date),
                createQrAnswer(birthDate),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_disease),
                createQrAnswer(disease),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_vaccine),
                createQrAnswer(vaccin),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_vaccine_type),
                createQrAnswer(vaccinType),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_producer),
                createQrAnswer(manufacturer),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_doses),
                createQrAnswer(doses),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_vaccination_date),
                createQrAnswer(vaccinationDate),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_vaccinated_in),
                createQrAnswer(vaccinationCountry),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_certificate_issuer),
                createQrAnswer(issuer ?: ""),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_unique_certificate),
                createQrAnswer(uniqueCode ?: ""),
                resources.getString(R.string.qr_explanation_description_eu_vaccination_footer),
            ) as String)
        )
    }

    override fun getForEuropeanRecoveryQr(readEuropeanCredential: JSONObject): InfoScreen {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
        val recovery = dcc.getJSONArray("r").optJSONObject(0)

        val title = resources.getString(R.string.qr_explanation_title_eu)

        val fullName = "${dcc.optJSONObject("nam").getStringOrNull("fn")}, ${
            dcc.optJSONObject("nam").getStringOrNull("gn")
        }"

        val birthDate = dcc.getStringOrNull("dob")?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val disease = resources.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val testDate = recovery.getStringOrNull("fr")?.let { testDate ->
            try {
                LocalDate.parse(testDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val country = getCountry(recovery.getStringOrNull("co"), getCurrentLocale())

        val producer = recovery.getStringOrNull("is")

        val validFromDate = recovery.getStringOrNull("df")?.let { testDate ->
            try {
                LocalDate.parse(testDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val validUntilDate = recovery.getStringOrNull("du")?.let { testDate ->
            try {
                LocalDate.parse(testDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val uniqueCode = recovery.getStringOrNull("ci")

        return InfoScreen(
            title = title,
            description = (TextUtils.concat(
                resources.getString(R.string.qr_explanation_description_eu_recovery_header),
                "<br/><br/>",
                resources.getString(R.string.qr_explanation_description_eu_recovery_name),
                createQrAnswer(fullName),
                resources.getString(R.string.qr_explanation_description_eu_recovery_birth_date),
                createQrAnswer(birthDate),
                resources.getString(R.string.qr_explanation_description_eu_recovery_disease),
                createQrAnswer(disease),
                resources.getString(R.string.qr_explanation_description_eu_recovery_test_date),
                createQrAnswer(testDate),
                resources.getString(R.string.qr_explanation_description_eu_recovery_country),
                createQrAnswer(country),
                resources.getString(R.string.qr_explanation_description_eu_recovery_producer),
                createQrAnswer(producer ?: ""),
                resources.getString(R.string.qr_explanation_description_eu_recovery_valid_from_date),
                createQrAnswer(validFromDate),
                resources.getString(R.string.qr_explanation_description_eu_recovery_valid_until_date),
                createQrAnswer(validUntilDate),
                resources.getString(R.string.qr_explanation_description_eu_recovery_unique_code),
                createQrAnswer(uniqueCode ?: ""),
                resources.getString(R.string.qr_explanation_description_eu_recovery_footer)
            ) as String)
        )
    }

    private fun createQrAnswer(answer: String): String =
        "<br/><b>$answer</b><br/><br/>"

    companion object {
        private const val issuerVWS = "Ministry of Health Welfare and Sport"
    }
}

