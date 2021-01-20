package nl.rijksoverheid.ctr

import nl.rijksoverheid.ctr.data.factory.DependencyFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class Tools {

    private val api = DependencyFactory().getTestApiClient()

    suspend fun checkSignatureValid() {

    }

}
