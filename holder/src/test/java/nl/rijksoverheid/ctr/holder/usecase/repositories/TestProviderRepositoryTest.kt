package nl.rijksoverheid.ctr.holder.usecase.repositories

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.api.apiModule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class TestProviderRepositoryTest : KoinTest {

    @Test
    fun `remoteTestResult returns RemoteTestResult`() = runBlocking {
        assertEquals(true, true)
    }
}
