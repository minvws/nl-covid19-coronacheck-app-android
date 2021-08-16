package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import android.os.Build
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearNumerical
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

interface InfoScreenUtil {
    fun getForRemoteTestResult2(
        result: RemoteTestResult2.Result,
        personalDetails: PersonalDetails,
        testDate: String
    ): InfoScreen

    fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String
    ): InfoScreen

    fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
    ): InfoScreen

    fun getForPositiveTest(
        event: RemoteEventPositiveTest,
        testDate: String,
        fullName: String,
        birthDate: String
    ): InfoScreen

    fun getForRecovery(
        event: RemoteEventRecovery,
        testDate: String,
        fullName: String,
        birthDate: String
    ): InfoScreen

    fun getForDomesticQr(personalDetails: PersonalDetails): InfoScreen
    fun getForEuropeanTestQr(readEuropeanCredential: JSONObject): InfoScreen
    fun getForEuropeanVaccinationQr(readEuropeanCredential: JSONObject): InfoScreen
    fun getForEuropeanRecoveryQr(readEuropeanCredential: JSONObject): InfoScreen

    fun getCountry(countryCode: String?, currentLocale: Locale?): String
}

class InfoScreenUtilImpl(
    private val application: Application,
    private val vaccinationInfoScreenUtil: VaccinationInfoScreenUtil,
    cachedAppConfigUseCase: CachedAppConfigUseCase
) : InfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForRemoteTestResult2(
        result: RemoteTestResult2.Result,
        personalDetails: PersonalDetails,
        testDate: String
    ): InfoScreen {
        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(
            R.string.your_test_result_explanation_description,
            "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}",
            result.testType,
            testDate,
            application.getString(R.string.your_test_result_explanation_negative_test_result),
            result.unique
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String
    ): InfoScreen {

        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == event.negativeTest?.type
        }?.name ?: event.negativeTest?.type ?: ""

        val testName = event.negativeTest?.name ?: ""

        val testLocation = event.negativeTest?.facility ?: ""

        val testManifacturer =
            holderConfig.euTestManufacturers.firstOrNull {
                it.code == event.negativeTest?.manufacturer
            }?.name ?: event.negativeTest?.manufacturer ?: ""

        val unique = event.unique ?: ""

        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(
            R.string.your_test_result_3_0_explanation_description,
            fullName,
            birthDate,
            testType,
            testName,
            testDate,
            application.getString(R.string.your_test_result_explanation_negative_test_result),
            testLocation,
            testManifacturer,
            unique
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
    ): InfoScreen {
        return vaccinationInfoScreenUtil.getForVaccination(event, fullName, birthDate, providerIdentifier)
    }

    override fun getForPositiveTest(
        event: RemoteEventPositiveTest,
        testDate: String,
        fullName: String,
        birthDate: String
    ): InfoScreen {

        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == event.positiveTest?.type
        }?.name ?: event.positiveTest?.type ?: ""

        val testName = event.positiveTest?.name ?: ""

        val testLocation = event.positiveTest?.facility ?: ""

        val testManifacturer =
            holderConfig.euTestManufacturers.firstOrNull {
                it.code == event.positiveTest?.manufacturer
            }?.name ?: event.positiveTest?.manufacturer ?: ""

        val unique = event.unique ?: ""

        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(
            R.string.your_test_result_3_0_explanation_description,
            fullName,
            birthDate,
            testType,
            testName,
            testDate,
            application.getString(R.string.your_test_result_explanation_positive_test_result),
            testLocation,
            testManifacturer,
            unique
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForRecovery(
        event: RemoteEventRecovery,
        testDate: String,
        fullName: String,
        birthDate: String
    ): InfoScreen {

        val validFromDate = event.recovery?.validFrom?.formatDayMonthYear() ?: ""
        val validUntilDate = event.recovery?.validUntil?.formatDayMonthYear() ?: ""

        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(
            R.string.recovery_explanation_description,
            fullName,
            birthDate,
            testDate,
            validFromDate,
            validUntilDate,
            event.unique
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }



    override fun getForDomesticQr(personalDetails: PersonalDetails): InfoScreen {
        val title = application.getString(R.string.qr_explanation_title_domestic)
        val description = application.getString(
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
                    .toLocalDate().formatDayMonthYearNumerical()
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val testResult =
            application.getString(R.string.your_test_result_explanation_negative_test_result)

        val testLocation = test.getStringOrNull("tc") ?: ""

        val manufacturer =
            holderConfig.euManufacturers.firstOrNull {
                it.code == test.getStringOrNull("ma")
            }?.name ?: test.getStringOrNull("ma") ?: ""

        val vaccinationCountry = getCountry(test.getStringOrNull("co"), getCurrentLocale())

        val issuerValue = test.getStringOrNull("is")
        val issuer = if (issuerValue == issuerVWS) {
            application.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val uniqueCode = test.getStringOrNull("ci")

        val description = application.getString(
            R.string.qr_explanation_description_eu_test,
            fullName,
            birthDate,
            disease,
            testType,
            testName,
            testDate,
            testResult,
            testLocation,
            manufacturer,
            vaccinationCountry,
            issuer,
            uniqueCode
        )

        return InfoScreen(
            title = title,
            description = description
        )
    }

    private fun getCurrentLocale(): Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        application.resources.configuration.locales[0]
    } else {
        application.resources.configuration.locale
    }

    override fun getCountry(
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

        val title = application.getString(R.string.qr_explanation_title_eu)

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
            if (vaccination.getStringOrNull("dn") != null && vaccination.getStringOrNull("sd") != null) {
                application.getString(
                    R.string.your_vaccination_explanation_doses_answer,
/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
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
            application.getString(R.string.qr_explanation_certificate_issuer)
        } else {
            issuerValue
        }

        val uniqueCode = vaccination.getStringOrNull("ci")

        return InfoScreen(
            title = title,
            description = application.getString(
                R.string.qr_explanation_description_eu_vaccination,
                fullName,
                birthDate,
                disease,
                vaccin,
                vaccinType,
                manufacturer,
                doses,
                vaccinationDate,
                vaccinationCountry,
                issuer,
                uniqueCode
            )
        )
    }

    override fun getForEuropeanRecoveryQr(readEuropeanCredential: JSONObject): InfoScreen {
        val dcc = readEuropeanCredential.optJSONObject("dcc")
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
            description = application.getString(
                R.string.qr_explanation_description_eu_recovery,
                fullName,
                birthDate,
                disease,
                testDate,
                country,
                producer,
                validFromDate,
                validUntilDate,
                uniqueCode
            )
        )
    }

    companion object {
        private const val issuerVWS = "Ministry of Health Welfare and Sport"
    }

}

@Parcelize
data class InfoScreen(
    val title: String,
    val description: String
): Parcelable