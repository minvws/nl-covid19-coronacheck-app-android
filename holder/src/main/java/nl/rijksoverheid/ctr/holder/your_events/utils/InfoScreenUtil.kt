/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.your_events.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.get_events.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.TestInfoScreenUtil
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
        isPaperProof: Boolean,
        addExplanation: Boolean = true,
    ): InfoScreen

    fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true,
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
        isPaperProof: Boolean,
        addExplanation: Boolean = true,
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
        isPaperProof: Boolean,
        addExplanation: Boolean,
    ) = testInfoScreenUtil.getForNegativeTest(event, fullName, testDate, birthDate, isPaperProof, addExplanation)


    override fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean,
    ) = vaccinationInfoScreenUtil.getForVaccination(event, fullName, birthDate, providerIdentifier, europeanCredential, addExplanation)

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
        isPaperProof: Boolean,
        addExplanation: Boolean,
    ) = recoveryInfoScreenUtil.getForRecovery(event, testDate, fullName, birthDate, isPaperProof, addExplanation)

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