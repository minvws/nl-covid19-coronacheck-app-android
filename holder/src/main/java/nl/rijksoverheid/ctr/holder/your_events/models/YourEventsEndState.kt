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
    object NegativeTestResultAddedAndNowAddVisitorAssessment : YourEventsEndState()
}

sealed class YourEventsEndStateWithCustomTitle(@StringRes val title: Int, @StringRes val description: Int) : YourEventsEndState() {
    object InternationalQROnly : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateInternationalQROnly_title,
        R.string.holder_listRemoteEvents_endStateInternationalQROnly_message)
    object VaccinationsAndRecovery : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_title,
        R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_message)
    object InternationalVaccinationAndRecovery : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateInternationalVaccinationAndRecovery_title,
        R.string.holder_listRemoteEvents_endStateInternationalVaccinationAndRecovery_message)
    object RecoveryOnly : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateRecoveryOnly_title,
        R.string.holder_listRemoteEvents_endStateRecoveryOnly_message)
    object NoRecoveryButDosisCorrection : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateNoRecoveryButDosisCorrection_title,
        R.string.holder_listRemoteEvents_endStateNoRecoveryButDosisCorrection_message)
    object RecoveryTooOld : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateRecoveryTooOld_title,
        R.string.holder_listRemoteEvents_endStateRecoveryTooOld_message)
    object RecoveryAndDosisCorrection : YourEventsEndStateWithCustomTitle(
        R.string.holder_listRemoteEvents_endStateRecoveryAndDosisCorrection_title,
        R.string.holder_listRemoteEvents_endStateRecoveryAndDosisCorrection_message)

    object WeCouldntMakeACertificate : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateCantCreateCertificate_title, R.string.holder_listRemoteEvents_endStateCantCreateCertificate_message)
    object UnknownHintCombination : YourEventsEndStateWithCustomTitle(R.string.holder_listRemoteEvents_endStateCantCreateCertificate_title, R.string.rule_engine_no_test_origin_description_retrieved_details)
}
