package nl.rijksoverheid.ctr.holder

import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.MissingOriginException
import nl.rijksoverheid.ctr.shared.models.Step
import java.lang.Exception

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
object MissingOriginErrorResult: ErrorResult {
    override fun getCurrentStep(): Step {
        return HolderStep.GetCredentialsNetworkRequest
    }

    override fun getException(): Exception {
        return MissingOriginException()
    }
}
