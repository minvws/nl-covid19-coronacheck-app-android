package nl.rijksoverheid.ctr.shared.exceptions


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class NoProvidersException(val errorCode: String): Exception(errorCode) {
    object Test: NoProvidersException("080")
    object Recovery: NoProvidersException("081")
    object Vaccination: NoProvidersException("082")
}
