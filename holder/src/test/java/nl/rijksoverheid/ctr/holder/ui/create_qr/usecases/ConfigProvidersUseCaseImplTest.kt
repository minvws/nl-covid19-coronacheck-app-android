package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.fakeCoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import org.junit.Assert.assertEquals
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ConfigProvidersUseCaseImplTest {

    private val testProvider1 = RemoteConfigProviders.TestProvider(
        name = "dummy",
        providerIdentifier = "1",
        resultUrl = "dummy",
        publicKey = "dummy".toByteArray()
    )
    private val testProvider2 = RemoteConfigProviders.TestProvider(
        name = "dummy",
        providerIdentifier = "2",
        resultUrl = "dummy",
        publicKey = "dummy".toByteArray()
    )

    @Test
    fun `Existing test provider should return one`() = runBlocking {
        val usecase = ConfigProvidersUseCaseImpl(
            coronaCheckRepository = fakeCoronaCheckRepository(
                testProviders = RemoteConfigProviders(
                    listOf(testProvider1, testProvider2),
                    listOf()
                )
            )
        )
        assertEquals(testProvider1, usecase.testProvider("1"))
    }

    @Test
    fun `Non-existing test provider should return null`() = runBlocking {
        val usecase = ConfigProvidersUseCaseImpl(
            coronaCheckRepository = fakeCoronaCheckRepository(
                testProviders = RemoteConfigProviders(
                    listOf(testProvider1, testProvider2),
                    listOf()
                )
            )
        )
        assertEquals(null, usecase.testProvider("3"))
    }
}
