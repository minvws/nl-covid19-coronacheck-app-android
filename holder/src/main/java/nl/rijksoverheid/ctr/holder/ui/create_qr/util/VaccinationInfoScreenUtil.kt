package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.content.res.Resources
import android.text.TextUtils
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface VaccinationInfoScreenUtil {

    fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
    ): InfoScreen
}

class VaccinationInfoScreenUtilImpl(
    private val lastVaccinationDoseUtil: LastVaccinationDoseUtil,
    private val resources: Resources,
    cachedAppConfigUseCase: CachedAppConfigUseCase
) : VaccinationInfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
    ): InfoScreen {
        val title = resources.getString(R.string.your_vaccination_explanation_toolbar_title)

        val name = resources.getString(R.string.your_vaccination_explanation_name)

        val birthDateQuestion =
            resources.getString(R.string.your_vaccination_explanation_birthday)

        val disease = resources.getString(R.string.your_vaccination_explanation_covid_19)
        val diseaseAnswer =
            resources.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val hpkCode = holderConfig.hpkCodes.firstOrNull { it.code == event.vaccination?.hpkCode }

        val vaccine = resources.getString(R.string.your_vaccination_explanation_vaccine)
        val vaccineAnswer = getVaccineAnswer(hpkCode, event)

        val vaccineType = resources.getString(R.string.your_vaccination_explanation_vaccine_type)
        val vaccineTypeAnswer = getVaccineTypeAnswer(hpkCode, event)

        val producer = resources.getString(R.string.your_vaccination_explanation_producer)
        val producerAnswer = getProducerAnswer(hpkCode, event)

        val doses = resources.getString(R.string.your_vaccination_explanation_doses)
        val dosesAnswer = getDosesAnswer(event, event.vaccination)

        val lastDose = resources.getString(R.string.your_vaccination_explanation_last_dose)
        val lastDoseAnswer = lastVaccinationDoseUtil.getIsLastDoseAnswer(event)

        val vaccinationDate =
            resources.getString(R.string.your_vaccination_explanation_vaccination_date)
        val vaccinationDateAnswer = event.vaccination?.date?.formatDayMonthYear() ?: ""

        val fullCountryName = if (event.vaccination?.country != null) {
            getFullCountryName(Locale.getDefault().language, event.vaccination.country)
        } else {
            ""
        }

        val vaccinationCountry =
            resources.getString(R.string.your_vaccination_explanation_vaccination_country)

        val uniqueCode = resources.getString(R.string.your_vaccination_explanation_unique_code)
        val uniqueCodeAnswer = event.unique ?: ""

        return InfoScreen(
            title = title,
            description = (TextUtils.concat(
                resources.getString(R.string.your_vaccination_explanation_header, providerIdentifier),
                "<br/><br/>",
                createdLine(name, fullName),
                createdLine(birthDateQuestion, birthDate, isOptional = true),
                "<br/>",
                createdLine(disease, diseaseAnswer),
                createdLine(vaccine, vaccineAnswer),
                createdLine(vaccineType, vaccineTypeAnswer),
                createdLine(producer, producerAnswer),
                createdLine(doses, dosesAnswer, isOptional = true),
                createdLine(lastDose, lastDoseAnswer, isOptional = true),
                createdLine(vaccinationDate, vaccinationDateAnswer, isOptional = true),
                createdLine(vaccinationCountry, fullCountryName, isOptional = true),
                createdLine(uniqueCode, uniqueCodeAnswer)
            ) as String)
        )
    }

    private fun getDosesAnswer(
        event: RemoteEventVaccination,
        vaccination: RemoteEventVaccination.Vaccination?
    ) = if (event.vaccination?.doseNumber != null && vaccination?.totalDoses != null) {
        resources.getString(
            R.string.your_vaccination_explanation_doses_answer,
            vaccination.doseNumber,
            vaccination.totalDoses
        )
    } else ""

    private fun getProducerAnswer(
        hpkCode: AppConfig.HpkCode?,
        event: RemoteEventVaccination
    ) =
        (holderConfig.euManufacturers.firstOrNull { it.code == event.vaccination?.manufacturer }?.name
            ?: holderConfig.euManufacturers.firstOrNull { it.code == hpkCode?.ma }?.name
            ?: event.vaccination?.manufacturer
            ?: "")

    private fun getVaccineTypeAnswer(
        hpkCode: AppConfig.HpkCode?,
        event: RemoteEventVaccination
    ) = (holderConfig.euVaccinations.firstOrNull { it.code == event.vaccination?.type }?.name
        ?: holderConfig.euVaccinations.firstOrNull { it.code == hpkCode?.vp }?.name
        ?: event.vaccination?.type
        ?: "")

    private fun getVaccineAnswer(
        hpkCode: AppConfig.HpkCode?,
        event: RemoteEventVaccination,
    ): String {
        val hpkCodeName = hpkCode?.name ?: ""
        val brand =
            holderConfig.euBrands.firstOrNull { it.code == event.vaccination?.brand }?.name
                ?: holderConfig.euBrands.firstOrNull { it.code == hpkCode?.mp }?.name
                ?: event.vaccination?.brand
                ?: ""
        return when {
            hpkCodeName.isNotEmpty() -> hpkCodeName
            brand.isNotEmpty() -> brand
            else -> ""
        }
    }

    private fun getFullCountryName(currentDeviceLanguage: String, currentCountryIso3Code: String): String {
        val countriesMap: MutableMap<String, String> = mutableMapOf()
        Locale.getISOCountries().forEach {
            val locale = Locale(currentDeviceLanguage, it)
            val countryIso3Code = locale.isO3Country
            val fullCountryName = locale.displayCountry
            countriesMap[countryIso3Code] = fullCountryName
        }

        return countriesMap[currentCountryIso3Code] ?: ""
    }

    private fun createdLine(
        name: String,
        nameAnswer: String,
        isOptional: Boolean = false
    ): String {
        return if (isOptional && nameAnswer.isEmpty()) "" else "$name <b>$nameAnswer</b><br/>"
    }
}