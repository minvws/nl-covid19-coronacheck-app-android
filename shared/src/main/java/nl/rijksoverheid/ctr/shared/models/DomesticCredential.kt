package nl.rijksoverheid.ctr.shared.models

import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class DomesticCredential(
    val credential: JSONObject,
    val attributes: DomesticCredentialAttributes
)
