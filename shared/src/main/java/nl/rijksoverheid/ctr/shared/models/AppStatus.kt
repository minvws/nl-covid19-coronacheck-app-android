package nl.rijksoverheid.ctr.shared.models

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class AppStatus {
    object ShouldUpdate : AppStatus()
    object AppDeactivated: AppStatus()
    object Ok: AppStatus()
}
