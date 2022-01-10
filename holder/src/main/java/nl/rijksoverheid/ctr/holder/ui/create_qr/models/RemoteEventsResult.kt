/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import nl.rijksoverheid.ctr.shared.models.ErrorResult

sealed class RemoteEventsResult {

    data class Success(val signedModel: SignedResponseWithModel<RemoteProtocol3>) :
        RemoteEventsResult()

    data class Error(val errorResult: ErrorResult) : RemoteEventsResult()
}