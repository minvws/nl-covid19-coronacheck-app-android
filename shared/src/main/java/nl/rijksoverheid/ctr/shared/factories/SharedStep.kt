package nl.rijksoverheid.ctr.shared.factories

import nl.rijksoverheid.ctr.shared.models.Step

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class SharedStep(override val code: Int) : Step(code) {
    object ConfigurationNetworkRequest : Step(10)
    object PublicKeysNetworkRequest : Step(20)
}
