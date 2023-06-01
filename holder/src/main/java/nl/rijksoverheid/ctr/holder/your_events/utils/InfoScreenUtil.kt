/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.your_events.utils

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination

interface InfoScreenUtil {

    fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true
    ): InfoScreen

    fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true
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
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true
    ): InfoScreen
}

class InfoScreenUtilImpl(
    private val vaccinationInfoScreenUtil: VaccinationInfoScreenUtil,
    private val testInfoScreenUtil: TestInfoScreenUtil,
    private val recoveryInfoScreenUtil: RecoveryInfoScreenUtil
) : InfoScreenUtil {

    override fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean
    ) = testInfoScreenUtil.getForNegativeTest(event, fullName, testDate, birthDate, europeanCredential, addExplanation)

    override fun getForVaccination(
        event: RemoteEventVaccination,
        fullName: String,
        birthDate: String,
        providerIdentifier: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean
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
        europeanCredential: ByteArray?,
        addExplanation: Boolean
    ) = recoveryInfoScreenUtil.getForRecovery(event, testDate, fullName, birthDate, europeanCredential, addExplanation)
}

@Parcelize
data class InfoScreen(
    val title: String,
    val description: String
) : Parcelable
