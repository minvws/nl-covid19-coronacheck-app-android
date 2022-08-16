package nl.rijksoverheid.ctr.holder.your_events.endstates

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

// extra endstates which are not fully covered by the hints system yet
sealed class Endstate(@StringRes title: Int, @StringRes description: Int) {
//    object noEndState: Endstate()
    object onlyAnInternationalCertificateCreated : Endstate(R.string.international_certificate_created_title, R.string.holder_listRemoteEvents_endStateCombinedFlowInternationalQROnly_message)
    object onlyARecoveryCertificateCreated : Endstate(R.string.international_certificate_created_title, R.string.international_certificate_created_title) // fix me
    object weCouldntMakeACertificate : Endstate(R.string.rule_engine_no_origin_title, R.string.rule_engine_no_test_origin_description)
    object vaccineAndRecoveryCertificateCreated : Endstate(R.string.certificate_created_vaccination_recovery_title, R.string.certificate_created_vaccination_recovery_description)
    object noRecoveryButVaccineCertificateCreated : Endstate(R.string.certificate_created_vaccination_title, R.string.certificate_created_vaccination_description)
    object positiveTestNoLongerValid : Endstate(R.string.cannot_create_recovery_proof_title, R.string.cannot_create_recovery_proof_description)
    object negativeTestResultAddedButNowAddVisitorAssessment : Endstate(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_title, R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_body)
//    object unknownHintCombination: Endstate()
}
