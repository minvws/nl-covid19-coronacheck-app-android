/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.verifier.models

import nl.rijksoverheid.ctr.shared.models.Step

sealed class VerifierStep(override val code: Int) : Step(code) {
    object ConfigurationNetworkRequest : VerifierStep(10)
    object PublicKeysNetworkRequest : VerifierStep(20)
}