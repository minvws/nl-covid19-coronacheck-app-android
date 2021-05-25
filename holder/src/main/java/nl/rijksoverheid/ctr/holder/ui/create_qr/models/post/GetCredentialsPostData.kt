package nl.rijksoverheid.ctr.holder.ui.create_qr.models.post

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class GetCredentialsPostData(
    val stoken: String,
    val events: List<String>,
    val issueCommitmentMessage: String
)
