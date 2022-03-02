/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.models

/**
 * Result state when sending vaccination with/without recovery events to the signer.
 */
sealed class YourEventFragmentEndState {

    /** A combined certificate after signing vaccination together with recovery events */
    data class CombinedVaccinationRecovery(val recoveryValidityDays: Int) :
        YourEventFragmentEndState()

    /** A vaccination certificate after signing vaccination together with recovery events */
    data class OnlyVaccination(val recoveryValidityDays: Int) :
        YourEventFragmentEndState()

    /** A recovery certificate after signing vaccination together with recovery events */
    object OnlyRecovery : YourEventFragmentEndState()

    /** No domestic certificate after signing vaccination together with recovery events */
    object InternationalWithRecovery : YourEventFragmentEndState()

    /** No domestic certificate after signing ONLY vaccination events (no recovery events) */
    object InternationalWithoutRecovery : YourEventFragmentEndState()

    object AddedNegativeTestInVaccinationAssessmentFlow: YourEventFragmentEndState()

    object NotApplicable : YourEventFragmentEndState()
}
