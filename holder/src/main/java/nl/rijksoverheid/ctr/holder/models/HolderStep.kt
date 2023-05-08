/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.models

import nl.rijksoverheid.ctr.shared.models.Step

sealed class HolderStep(override val code: Int) : Step(code) {
    object ConfigurationNetworkRequest : HolderStep(10)
    object DigidNetworkRequest : HolderStep(10)
    object CouplingNetworkRequest : HolderStep(10)
    object PublicKeysNetworkRequest : HolderStep(20)
    object TestProvidersNetworkRequest : HolderStep(20)
    object TestResultNetworkRequest : HolderStep(50)
    object ConfigProvidersNetworkRequest : HolderStep(20)
    object AccessTokensNetworkRequest : HolderStep(30)
    object UnomiNetworkRequest : HolderStep(40)
    object EventNetworkRequest : HolderStep(50)
    object StoringEvents : HolderStep(60)
    object PrepareIssueNetworkRequest : HolderStep(70)
    object GetCredentialsNetworkRequest : HolderStep(80)
    object StoringCredentials : HolderStep(90)
    object DataMigrationImport : HolderStep(10)
    object DataMigrationExport : HolderStep(20)
}
