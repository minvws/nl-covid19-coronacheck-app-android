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

sealed class CombinedVaccinationRecoveryResult {

    object IncompleteDomesticVaccination: CombinedVaccinationRecoveryResult()

    object CombinedVaccinationRecovery: CombinedVaccinationRecoveryResult()

    object OnlyVaccination: CombinedVaccinationRecoveryResult()

    object OnlyRecovery: CombinedVaccinationRecoveryResult()

    object IncompleteDomesticVaccinationWithRecovery: CombinedVaccinationRecoveryResult()

    object NotApplicable: CombinedVaccinationRecoveryResult()
}
