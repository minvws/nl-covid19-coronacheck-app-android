/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.res.Resources
import android.text.TextUtils
import java.util.Locale
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.LastVaccinationDoseUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase

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
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true
    ): InfoScreen
}

class VaccinationInfoScreenUtilImpl(
    private val lastVaccinationDoseUtil: LastVaccinationDoseUtil,
    private val resources: Resources,
    private val countryUtil: CountryUtil,
    private val paperProofUtil: PaperProofUtil,
    cachedAppConfigUseCase: HolderCachedAppConfigUseCase
) : VaccinationInfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean
    ): InfoScreen {
        val title =
            if (europeanCredential != null) resources.getString(R.string.your_vaccination_explanation_toolbar_title) else resources.getString(
                R.string.your_test_result_explanation_toolbar_title
            )

        val name = resources.getString(R.string.your_vaccination_explanation_name)

        val birthDateQuestion =
            resources.getString(R.string.your_vaccination_explanation_birthday)

        val disease = resources.getString(R.string.your_vaccination_explanation_covid_19)
        val diseaseAnswer =
            resources.getString(R.string.your_vaccination_explanation_covid_19_answer)

        val hpkCode = holderConfig.hpkCodes.firstOrNull { it.code == event.vaccination?.hpkCode }

        val vaccine = resources.getString(R.string.your_vaccination_explanation_vaccine)
        val vaccineAnswer = getVaccineAnswer(hpkCode, event)

        val vaccineDisplayName =
            resources.getString(R.string.holder_event_aboutVaccination_productName)
        val vaccineDisplayNameAnswer = hpkCode?.displayName ?: ""

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
            countryUtil.getCountryForInfoScreen(
                Locale.getDefault().language,
                event.vaccination.country
            )
        } else {
            ""
        }

        val vaccinationCountry =
            resources.getString(R.string.your_vaccination_explanation_vaccination_country)

        val uniqueCode = resources.getString(R.string.your_vaccination_explanation_unique_code)
        val uniqueCodeAnswer = event.unique ?: ""

        val header = if (europeanCredential != null) {
            resources.getString(R.string.paper_proof_event_explanation_header)
        } else {
            resources.getString(R.string.your_vaccination_explanation_header, providerIdentifier)
        }

        return InfoScreen(
            title = title,
            description = (TextUtils.concat(
                header,
                "<br/><br/>",
                createdLine(name, fullName),
                createdLine(birthDateQuestion, birthDate, isOptional = true),
                "<br/>",
                createdLine(disease, diseaseAnswer),
                createdLine(vaccine, vaccineAnswer),
                createdLine(vaccineDisplayName, vaccineDisplayNameAnswer),
                createdLine(vaccineType, vaccineTypeAnswer),
                createdLine(producer, producerAnswer),
                createdLine(doses, dosesAnswer, isOptional = true),
                createdLine(lastDose, lastDoseAnswer, isOptional = true),
                createdLine(vaccinationDate, vaccinationDateAnswer, isOptional = true),
                createdLine(vaccinationCountry, fullCountryName, isOptional = true),
                if (europeanCredential != null) {
                    val issuerAnswer = paperProofUtil.getIssuer(europeanCredential)
                    createdLine(
                        resources.getString(R.string.holder_dcc_issuer),
                        if (issuerAnswer == "Ministry of Health Welfare and Sport") {
                            resources.getString(R.string.qr_explanation_certificate_issuer)
                        } else {
                            issuerAnswer
                        },
                        isOptional = true
                    )
                } else {
                    ""
                },
                "<br/>",
                createdLine(uniqueCode, uniqueCodeAnswer),
                if (europeanCredential != null && addExplanation) {
                    paperProofUtil.getInfoScreenFooterText(europeanCredential)
                } else {
                    ""
                }
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
        event: RemoteEventVaccination
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

    private fun createdLine(
        name: String,
        nameAnswer: String,
        isOptional: Boolean = false
    ): String {
        return if (isOptional && nameAnswer.isEmpty()) "" else "$name <b>$nameAnswer</b><br/>"
    }
}
