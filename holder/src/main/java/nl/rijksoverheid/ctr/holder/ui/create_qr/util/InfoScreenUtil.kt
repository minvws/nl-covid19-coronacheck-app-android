package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.app.Application
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import java.lang.Exception
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface InfoScreenUtil {
    fun getForRemoteTestResult2(result: RemoteTestResult.Result,
                                personalDetails: PersonalDetails,
                                testDate: String): InfoScreen
    fun getForRemoteTestResult3(event: RemoteEventsNegativeTests.Event,
                                fullName: String,
                                testDate: String,
                                validUntil: String,
                                birthDate: String): InfoScreen
    fun getForRemoteVaccination(event: RemoteEventsVaccinations.Event,
                                fullName: String,
                                birthDate: String): InfoScreen
    fun getForDomesticQr(): InfoScreen
    fun getForEuropeanTestQr(): InfoScreen
    fun getForEuropeanVaccinationQr(): InfoScreen
}

class InfoScreenUtilImpl(private val application: Application,
                         private val cachedAppConfigUseCase: CachedAppConfigUseCase): InfoScreenUtil {
    override fun getForRemoteTestResult2(
        result: RemoteTestResult.Result,
        personalDetails: PersonalDetails,
        testDate: String): InfoScreen {
        val title = application.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = application.getString(R.string.your_test_result_explanation_description,
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

    override fun getForRemoteTestResult3(
        event: RemoteEventsNegativeTests.Event,
        fullName: String,
        testDate: String,
        validUntil: String,
        birthDate: String
    ): InfoScreen {

        val testType = cachedAppConfigUseCase.getCachedAppConfig()?.euTestTypes?.firstOrNull {
            it.code == event.negativeTest?.type
        }?.name ?: event.negativeTest?.type ?: ""

        val testName = event.negativeTest?.name ?: ""

        val testLocation = event.negativeTest?.facility ?: ""

        val testManifacturer = cachedAppConfigUseCase.getCachedAppConfig()?.euTestManufacturers?.firstOrNull {
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

    override fun getForRemoteVaccination(
        event: RemoteEventsVaccinations.Event,
        fullName: String,
        birthDate: String
    ): InfoScreen {
        val title = application.getString(R.string.your_vaccination_explanation_toolbar_title)

        val desease = application.getString(R.string.your_vaccination_explanation_covid_19)

        val hpkCode = cachedAppConfigUseCase.getCachedAppConfig()?.hpkCodes?.firstOrNull {
            it.code == event.vaccination?.hpkCode }?.name ?: event.vaccination?.hpkCode ?: ""

        val brand = cachedAppConfigUseCase.getCachedAppConfig()?.euBrands?.firstOrNull {
            it.code == event.vaccination?.brand }?.name ?: event.vaccination?.brand ?: ""

        val vaccin = when {
            hpkCode.isNotEmpty() -> hpkCode
            brand.isNotEmpty() -> brand
            else -> ""
        }

        val vaccinType = cachedAppConfigUseCase.getCachedAppConfig()?.euVaccinations?.firstOrNull {
            it.code == event.vaccination?.type }?.name ?: event.vaccination?.type ?: ""

        val producer = cachedAppConfigUseCase.getCachedAppConfig()?.euManufacturers?.firstOrNull {
            it.code == event.vaccination?.manufacturer }?.name ?: event.vaccination?.manufacturer ?: ""

        val doses = if (event.vaccination?.doseNumber != null && event.vaccination?.totalDoses != null) {
            application.getString(R.string.your_vaccination_explanation_doses, event.vaccination?.doseNumber, event.vaccination?.totalDoses)
        } else ""

        val vaccinationDate = event.vaccination?.date?.let { it.formatDayMonthYear() } ?: ""
        val vaccinationCountry = event.vaccination?.country ?: ""
        val uniqueCode = event.unique ?: ""

        return InfoScreen(
            title = title,
            description = application.getString(
                R.string.your_vaccination_explanation_description,
                fullName,
                birthDate,
                desease,
                vaccin,
                vaccinType,
                producer,
                doses,
                vaccinationDate,
                vaccinationCountry,
                uniqueCode
            )
        )

    }

    override fun getForDomesticQr(): InfoScreen {
        TODO("Not yet implemented")
    }

    override fun getForEuropeanTestQr(): InfoScreen {
        TODO("Not yet implemented")
    }

    override fun getForEuropeanVaccinationQr(): InfoScreen {
        TODO("Not yet implemented")
    }

}

data class InfoScreen(
    val title: String,
    val description: String
)