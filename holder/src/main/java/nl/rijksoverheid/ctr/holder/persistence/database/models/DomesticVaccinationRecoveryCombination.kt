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
sealed class DomesticVaccinationRecoveryCombination {

    /** A combined certificate after signing vaccination together with recovery events */
    data class CombinedVaccinationRecovery(val recoveryValidityDays: Int) :
        DomesticVaccinationRecoveryCombination()

    /** A vaccination certificate after signing vaccination together with recovery events */
    data class OnlyVaccination(val recoveryValidityDays: Int) :
        DomesticVaccinationRecoveryCombination()

    /** A recovery certificate after signing vaccination together with recovery events */
    object OnlyRecovery : DomesticVaccinationRecoveryCombination()

    /** No domestic certificate after signing vaccination together with recovery events */
    object NoneWithRecovery : DomesticVaccinationRecoveryCombination()

    /** No domestic certificate after signing ONLY vaccination events (no recovery events) */
    object NoneWithoutRecovery : DomesticVaccinationRecoveryCombination()

    object NotApplicable : DomesticVaccinationRecoveryCombination()
}
