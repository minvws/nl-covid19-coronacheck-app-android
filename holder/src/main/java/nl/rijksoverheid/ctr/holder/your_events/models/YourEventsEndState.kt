/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.models

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R

sealed class YourEventsEndState {
    object None : YourEventsEndState()
    data class Hints(val localisedHints: List<String>) : YourEventsEndState()
    object NegativeTestResultAddedButNowAddVisitorAssessment : YourEventsEndState()
}

sealed class YourEventsEndStateWithCustomTitle(@StringRes title: Int, @StringRes description: Int) : YourEventsEndState() {
    object OnlyAnInternationalCertificateCreated : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateInternationalQROnly_title, R.string.holder_listRemoteEvents_endStateInternationalQROnly_message)
    object WeCouldntMakeACertificate : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateCantCreateCertificate_title, R.string.holder_listRemoteEvents_endStateCantCreateCertificate_message)
    object VaccineAndRecoveryCertificateCreated : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_title, R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_message)
    object OnlyARecoveryCertificateCreated : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateRecoveryOnly_title, R.string.holder_listRemoteEvents_endStateRecoveryOnly_message)
    object ARecoveryCertificateCreated : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateRecoveryAndDosisCorrection_title, R.string.holder_listRemoteEvents_endStateRecoveryAndDosisCorrection_message)
//    object WeCouldntMakeARecoveryCertificateCreate: YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateNoRecoveryButDosisCorrection_title, R.string.holder_listRemoteEvents_endStateNoRecoveryButDosisCorrection_message)
    object NoRecoveryButVaccineCertificateCreated : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateNoRecoveryButDosisCorrection_title, R.string.holder_listRemoteEvents_endStateNoRecoveryButDosisCorrection_message)
    object PositiveTestNoLongerValid : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateRecoveryTooOld_title, R.string.holder_listRemoteEvents_endStateRecoveryTooOld_message)
}
