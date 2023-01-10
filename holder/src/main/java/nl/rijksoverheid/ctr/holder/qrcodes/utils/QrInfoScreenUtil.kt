/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.qrcodes.utils

import android.app.Application
import android.text.TextUtils
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import nl.rijksoverheid.ctr.design.ext.formatDateTimeWithTimeZone
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearNumerical
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtil
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import nl.rijksoverheid.ctr.shared.ext.locale
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import org.json.JSONObject

interface QrInfoScreenUtil {
    fun getForDomesticQr(
        personalDetails: PersonalDetails,
        disclosurePolicy: GreenCardDisclosurePolicy
    ): QrInfoScreen

    fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): QrInfoScreen
    fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): QrInfoScreen
    fun getForEuropeanRecoveryQr(readEuropeanCredential: JSONObject): QrInfoScreen
}

class QrInfoScreenUtilImpl(
    private val application: Application,
    private val readEuropeanCredentialUtil: ReadEuropeanCredentialUtil,
    private val countryUtil: CountryUtil,
    private val localDateUtil: LocalDateUtil,
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase
) : QrInfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForDomesticQr(
        personalDetails: PersonalDetails,
        disclosurePolicy: GreenCardDisclosurePolicy
    ): QrInfoScreen {
        val title = application.getString(R.string.qr_explanation_title_domestic)

        val description = application.getString(
            when (disclosurePolicy) {
                is GreenCardDisclosurePolicy.OneG -> {
                    R.string.holder_qr_explanation_description_domestic_1G
                }
                is GreenCardDisclosurePolicy.ThreeG -> {
                    R.string.qr_explanation_description_domestic
                }
            },
            "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
        )

        return QrInfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): QrInfoScreen {
        val dcc = requireNotNull(readEuropeanCredential.optJSONObject("dcc"))
        val test = dcc.getJSONArray("t").optJSONObject(0)

        val title = application.getString(R.string.qr_explanation_title_eu)

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

        val disease = application.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == test.getStringOrNull("tt")
        }?.name ?: test.getStringOrNull("tt") ?: ""

        val testName = test.getStringOrNull("nm") ?: ""

        val testDate = test.getStringOrNull("sc")?.let {
            try {
                OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .formatDateTimeWithTimeZone(application)
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val testResult =
            application.getString(R.string.holder_showqr_eu_about_test_negative)

        val testLocation = test.getStringOrNull("tc") ?: ""

        val manufacturer =
            holderConfig.euTestManufacturers.firstOrNull {
                it.code == test.getStringOrNull("ma")
            }?.name ?: test.getStringOrNull("ma") ?: ""

        val testCountry =
            countryUtil.getCountryForQrInfoScreen(
                test.getStringOrNull("co"),
                application.applicationContext.locale()
            )

        val issuerValue = test.getStringOrNull("is")
        val issuer = if (issuerValue == issuerVWS) {
            application.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val uniqueCode = test.getStringOrNull("ci")

        val texts = mutableListOf(
            application.getString(R.string.qr_explanation_description_eu_test_header),
            "<br/><br/>",
            application.getString(R.string.qr_explanation_description_eu_test_name),
            createQrAnswer(fullName),
            application.getString(R.string.qr_explanation_description_eu_test_birth_date),
            createQrAnswer(birthDate),
            application.getString(R.string.qr_explanation_description_eu_test_disease),
            createQrAnswer(disease)
        )

        if (testType.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_test_type),
                    createQrAnswer(testType)
                )
            )
        }

        if (testName.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_test_name),
                    createQrAnswer(testName)
                )
            )
        }

        if (testDate.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_test_date),
                    createQrAnswer(testDate)
                )
            )
        }

        if (testResult.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_test_result),
                    createQrAnswer(testResult)
                )
            )
        }

        if (manufacturer.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_manufacturer),
                    createQrAnswer(manufacturer)
                )
            )
        }

        if (testLocation.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_test_centre),
                    createQrAnswer(testLocation)
                )
            )
        }

        if (testCountry.isNotBlank()) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_test_country),
                    createQrAnswer(testCountry)
                )
            )
        }

        if (issuer?.isNotBlank() == true) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_issuer),
                    createQrAnswer(issuer)
                )
            )
        }

        if (uniqueCode?.isNotBlank() == true) {
            texts.addAll(
                listOf(
                    application.getString(R.string.qr_explanation_description_eu_test_certificate_identifier),
                    createQrAnswer(uniqueCode)
                )
            )
        }

        return QrInfoScreen(
            title = title,
            description = (TextUtils.concat(*texts.toTypedArray()) as String),
            footer = application.getString(R.string.qr_explanation_description_eu_test_footer)
        )
    }

    override fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): QrInfoScreen {
        val dcc = requireNotNull(readEuropeanCredential.optJSONObject("dcc"))
        val vaccination = dcc.getJSONArray("v").optJSONObject(0)

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

        val disease = application.getString(R.string.your_vaccination_explanation_covid_19_answer)

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
            readEuropeanCredentialUtil.getDoseRangeStringForVaccination(readEuropeanCredential)
        val overDoseLink =
            if (readEuropeanCredentialUtil.doseExceedsTotalDoses(readEuropeanCredential)) {
                application.getString(R.string.holder_showqr_eu_about_vaccination_dosage_message)
            } else ""

        val (vaccinationDate, vaccinationDays) = vaccination.getStringOrNull("dt")
            ?.let { vaccinationDate ->
                localDateUtil.dateAndDaysSince(vaccinationDate)
            } ?: Pair("", "")

        val countryCode = vaccination.getStringOrNull("co")
        val vaccinationCountry =
            countryUtil.getCountryForQrInfoScreen(
                countryCode,
                application.applicationContext.locale()
            )

        val issuerValue = vaccination.getStringOrNull("is")
        val issuer = if (issuerValue == issuerVWS) {
            application.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val uniqueCode = vaccination.getStringOrNull("ci")

        val title = application.getString(R.string.qr_explanation_title_eu_vaccination, doses)

        return QrInfoScreen(
            title = title,
            description = (TextUtils.concat(
                application.getString(R.string.qr_explanation_description_eu_vaccination_header),
                "<br/><br/>",
                application.getString(R.string.qr_explanation_description_eu_vaccination_name),
                createQrAnswer(fullName),
                application.getString(R.string.qr_explanation_description_eu_vaccination_birth_date),
                createQrAnswer(birthDate),
                application.getString(R.string.qr_explanation_description_eu_vaccination_disease),
                createQrAnswer(disease),
                application.getString(R.string.qr_explanation_description_eu_vaccination_vaccine),
                createQrAnswer(vaccin),
                application.getString(R.string.qr_explanation_description_eu_vaccination_vaccine_type),
                createQrAnswer(vaccinType),
                application.getString(R.string.qr_explanation_description_eu_vaccination_producer),
                createQrAnswer(manufacturer),
                application.getString(R.string.qr_explanation_description_eu_vaccination_doses),
                createQrAnswer(doses, overDoseLink),
                application.getString(R.string.qr_explanation_description_eu_vaccination_vaccination_date),
                createQrAnswer(vaccinationDate),
                application.getString(R.string.holder_showQR_euAboutVaccination_daysSince),
                createQrAnswer(vaccinationDays),
                application.getString(R.string.qr_explanation_description_eu_vaccination_vaccinated_in),
                createQrAnswer(vaccinationCountry),
                application.getString(R.string.qr_explanation_description_eu_vaccination_certificate_issuer),
                createQrAnswer(issuer ?: ""),
                application.getString(R.string.qr_explanation_description_eu_vaccination_unique_certificate),
                createQrAnswer(uniqueCode ?: "")
            ) as String),
            footer = application.getString(R.string.qr_explanation_description_eu_vaccination_footer)
        )
    }

    override fun getForEuropeanRecoveryQr(readEuropeanCredential: JSONObject): QrInfoScreen {
        val dcc = requireNotNull(readEuropeanCredential.optJSONObject("dcc"))
        val recovery = dcc.getJSONArray("r").optJSONObject(0)

        val title = application.getString(R.string.qr_explanation_title_eu)

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

        val disease = application.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val testDate = recovery.getStringOrNull("fr")?.let { testDate ->
            try {
                LocalDate.parse(testDate, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val country = countryUtil.getCountryForQrInfoScreen(
            recovery.getStringOrNull("co"),
            application.applicationContext.locale()
        )

        val issuerValue = recovery.getStringOrNull("is")
        val issuer = if (issuerValue == issuerVWS) {
            application.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val validFromDate = recovery.getStringOrNull("df")?.let {
            try {
                LocalDate.parse(it, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val validUntilDate = recovery.getStringOrNull("du")?.let {
            try {
                LocalDate.parse(it, DateTimeFormatter.ISO_DATE).formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val uniqueCode = recovery.getStringOrNull("ci")

        return QrInfoScreen(
            title = title,
            description = (TextUtils.concat(
                application.getString(R.string.qr_explanation_description_eu_recovery_header),
                "<br/><br/>",
                application.getString(R.string.qr_explanation_description_eu_recovery_name),
                createQrAnswer(fullName),
                application.getString(R.string.qr_explanation_description_eu_recovery_birth_date),
                createQrAnswer(birthDate),
                application.getString(R.string.qr_explanation_description_eu_recovery_disease),
                createQrAnswer(disease),
                application.getString(R.string.qr_explanation_description_eu_recovery_test_date),
                createQrAnswer(testDate),
                application.getString(R.string.qr_explanation_description_eu_recovery_country),
                createQrAnswer(country),
                application.getString(R.string.qr_explanation_description_eu_recovery_producer),
                createQrAnswer(issuer ?: ""),
                application.getString(R.string.qr_explanation_description_eu_recovery_valid_from_date),
                createQrAnswer(validFromDate),
                application.getString(R.string.qr_explanation_description_eu_recovery_valid_until_date),
                createQrAnswer(validUntilDate),
                application.getString(R.string.qr_explanation_description_eu_recovery_unique_code),
                createQrAnswer(uniqueCode ?: "")
            ) as String),
            footer = application.getString(R.string.qr_explanation_description_eu_recovery_footer)
        )
    }

    private fun createQrAnswer(answer: String, answerDescription: String = ""): String =
        "<br/><b>$answer</b><br/>${if (answerDescription.isEmpty()) "<br/>" else "$answerDescription<br/><br/>"}"

    companion object {
        private const val issuerVWS = "Ministry of Health Welfare and Sport"
    }
}

data class QrInfoScreen(
    val title: String,
    val description: String,
    val footer: String = ""
)
