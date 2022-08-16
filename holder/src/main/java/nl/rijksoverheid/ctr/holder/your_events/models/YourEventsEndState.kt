/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.models

import nl.rijksoverheid.ctr.holder.R

interface EndStateTitleDescription {
    val titleDescription: Pair<Int, Int>
}

sealed class YourEventsEndState {
    object None : YourEventsEndState()
    data class Hints(val localisedHints: List<String>) : YourEventsEndState()
    object OnlyAnInternationalCertificateCreated : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.international_certificate_created_title, R.string.holder_listRemoteEvents_endStateCombinedFlowInternationalQROnly_message)
    }
    object OnlyARecoveryCertificateCreated : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.international_certificate_created_title, R.string.international_certificate_created_title) // fix me)
    }
    object WeCouldntMakeACertificate : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.rule_engine_no_origin_title, R.string.rule_engine_no_test_origin_description)
    }
    object VaccineAndRecoveryCertificateCreated : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.certificate_created_vaccination_recovery_title, R.string.certificate_created_vaccination_recovery_description)
    }
    object NoRecoveryButVaccineCertificateCreated : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.certificate_created_vaccination_title, R.string.certificate_created_vaccination_description)
    }
    object PositiveTestNoLongerValid : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.cannot_create_recovery_proof_title, R.string.cannot_create_recovery_proof_description)
    }
    object NegativeTestResultAddedButNowAddVisitorAssessment : YourEventsEndState(), EndStateTitleDescription {
        override val titleDescription: Pair<Int, Int>
            get() = Pair(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_title, R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_body)
    }
}
