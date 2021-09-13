/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Class to represent OpenID authentication triggered errors
 */
sealed class OpenIdErrorResult(open val step: Step, open val e: Exception) :
    ErrorResult {

    @Parcelize
    data class Error(override val step: Step, override val e: Exception) :
        OpenIdErrorResult(step, e), Parcelable {

        override fun getCurrentStep() = step

        override fun getException() = e
    }

    @Parcelize
    data class ServerBusy(override val step: Step, override val e: Exception) :
        OpenIdErrorResult(step, e), Parcelable {

        override fun getCurrentStep() = step

        override fun getException() = e
    }
}