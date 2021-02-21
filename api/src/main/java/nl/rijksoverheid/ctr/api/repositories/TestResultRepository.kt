package nl.rijksoverheid.ctr.api.repositories

import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TestResultRepository {

    // TODO: Fetch from remote
    suspend fun getTestValiditySeconds(): Long {
        return TimeUnit.HOURS.toSeconds(48)
    }
}
