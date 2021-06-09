package nl.rijksoverheid.ctr.holder.ui.create_qr.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class RemotePrepareIssue(
    val stoken: String,
    val prepareIssueMessage: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemotePrepareIssue

        if (stoken != other.stoken) return false
        if (!prepareIssueMessage.contentEquals(other.prepareIssueMessage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stoken.hashCode()
        result = 31 * result + prepareIssueMessage.contentHashCode()
        return result
    }
}
