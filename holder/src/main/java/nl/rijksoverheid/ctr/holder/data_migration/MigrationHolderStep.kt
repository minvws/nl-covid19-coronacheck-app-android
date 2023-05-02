/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.data_migration

import nl.rijksoverheid.ctr.shared.models.Step

sealed class MigrationHolderStep(override val code: Int) : Step(code) {
    object Import : Step(10)
    object Export : Step(20)
}
