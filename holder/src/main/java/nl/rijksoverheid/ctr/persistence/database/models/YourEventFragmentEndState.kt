/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.persistence.database.models

/**
 * Result state when sending vaccination with/without recovery events to the signer.
 */
sealed class YourEventFragmentEndState {

    /** A combined certificate after signing vaccination together with recovery events */
    data class CombinedVaccinationRecovery(val recoveryValidityDays: Int) :
        YourEventFragmentEndState()

    /** A domestic vaccination certificate after signing vaccination together with recovery events */
    data class OnlyDomesticVaccination(val recoveryValidityDays: Int) :
        YourEventFragmentEndState()

    /** A recovery certificate after signing vaccination together with recovery events */
    object OnlyRecovery : YourEventFragmentEndState()

    /** Vaccination and recovery certificate after signing vaccination together with recovery events */
    object VaccinationAndRecovery : YourEventFragmentEndState()

    /** No recovery certificate after signing vaccination and recovery events with already stored vaccinations  */
    object NoRecoveryWithStoredVaccination : YourEventFragmentEndState()

    /** No domestic certificate after signing vaccination events with or without recovery events */
    object OnlyInternationalVaccination : YourEventFragmentEndState()

    object AddedNegativeTestInVaccinationAssessmentFlow : YourEventFragmentEndState()

    object NotApplicable : YourEventFragmentEndState()
}
