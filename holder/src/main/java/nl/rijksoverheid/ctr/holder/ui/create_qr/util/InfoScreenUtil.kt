/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.models.PersonalDetails

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
        birthDate: String,
        isPaperProof: Boolean
    ): InfoScreen

    fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        isPaperProof: Boolean
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
        birthDate: String,
        isPaperProof: Boolean
    ): InfoScreen

    fun getForVaccinationAssessment(
        event: RemoteEventVaccinationAssessment,
        fullName: String,
        birthDate: String
    ): InfoScreen
}

class InfoScreenUtilImpl(
    private val vaccinationInfoScreenUtil: VaccinationInfoScreenUtil,
    private val testInfoScreenUtil: TestInfoScreenUtil,
    private val recoveryInfoScreenUtil: RecoveryInfoScreenUtil,
    private val vaccinationAssessmentInfoScreenUtil: VaccinationAssessmentInfoScreenUtil
) : InfoScreenUtil {

    override fun getForRemoteTestResult2(
        result: RemoteTestResult2.Result,
        personalDetails: PersonalDetails,
        testDate: String
    ) = testInfoScreenUtil.getForRemoteTestResult2(result, personalDetails, testDate)

    override fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String,
        isPaperProof: Boolean
    ) = testInfoScreenUtil.getForNegativeTest(event, fullName, testDate, birthDate, isPaperProof)


    override fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        isPaperProof: Boolean
    ) = vaccinationInfoScreenUtil.getForVaccination(event, fullName, birthDate, providerIdentifier, isPaperProof)

    override fun getForPositiveTest(
        event: RemoteEventPositiveTest,
        testDate: String,
        fullName: String,
        birthDate: String
    ) = testInfoScreenUtil.getForPositiveTest(event, testDate, fullName, birthDate)

    override fun getForRecovery(
        event: RemoteEventRecovery,
        testDate: String,
        fullName: String,
        birthDate: String,
        isPaperProof: Boolean
    ) = recoveryInfoScreenUtil.getForRecovery(event, testDate, fullName, birthDate, isPaperProof)

    override fun getForVaccinationAssessment(
        event: RemoteEventVaccinationAssessment,
        fullName: String,
        birthDate: String
    ) = vaccinationAssessmentInfoScreenUtil.getForVaccinationAssessment(event, fullName, birthDate)
}

@Parcelize
data class InfoScreen(
    val title: String,
    val description: String
) : Parcelable