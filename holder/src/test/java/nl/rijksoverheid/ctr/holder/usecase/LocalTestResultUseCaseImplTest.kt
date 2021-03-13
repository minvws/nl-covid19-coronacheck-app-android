package nl.rijksoverheid.ctr.holder.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.holder.fakeCachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.fakePersistenceManager
import nl.rijksoverheid.ctr.holder.fakePersonalDetailsUtil
import nl.rijksoverheid.ctr.holder.fakeTestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

class LocalTestResultUseCaseImplTest {

    @Test
    fun `No local test result saved returns None`() =
        runBlocking {
            val usecase = LocalTestResultUseCaseImpl(
                persistenceManager = fakePersistenceManager(credentials = null),
                testResultUtil = TestResultUtil(Clock.systemUTC()),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                testResultAttributesUseCase = fakeTestResultAttributesUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil()
            )

            val localTestResult = usecase.get(null)
            assertTrue(localTestResult is LocalTestResultState.None)
        }

    @Test
    fun `Local test result that is still valid saved returns Valid and does not clear persisted credentials`() =
        runBlocking {
            val mockedPersistenceManager: PersistenceManager = mockk(relaxed = true)
            every { mockedPersistenceManager.getCredentials() } answers { "" }

            val usecase = LocalTestResultUseCaseImpl(
                persistenceManager = mockedPersistenceManager,
                testResultUtil = TestResultUtil(
                    clock = Clock.fixed(Instant.parse("2021-01-02T00:00:00.00Z"), ZoneId.of("UTC"))
                ),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = AppConfig(
                        minimumVersion = 0,
                        appDeactivated = false,
                        informationURL = "dummy",
                        configTtlSeconds = 0,
                        maxValidityHours = 48
                    )
                ),
                testResultAttributesUseCase = fakeTestResultAttributesUseCase(
                    sampleTimeSeconds = OffsetDateTime.ofInstant(
                        Instant.parse("2021-01-01T00:00:00.00Z"),
                        ZoneId.of("UTC")
                    ).toEpochSecond()
                ),
                personalDetailsUtil = fakePersonalDetailsUtil()
            )

            val localTestResult = usecase.get(null)
            assertFalse((localTestResult as LocalTestResultState.Valid).firstTimeCreated)
            verify(exactly = 0) { mockedPersistenceManager.deleteCredentials() }
        }

    @Test
    fun `Local test result that is valid with previous test result state None returns first time created`() =
        runBlocking {
            val mockedPersistenceManager: PersistenceManager = mockk(relaxed = true)
            every { mockedPersistenceManager.getCredentials() } answers { "" }

            val usecase = LocalTestResultUseCaseImpl(
                persistenceManager = mockedPersistenceManager,
                testResultUtil = TestResultUtil(
                    clock = Clock.fixed(Instant.parse("2021-01-02T00:00:00.00Z"), ZoneId.of("UTC"))
                ),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                    appConfig = AppConfig(
                        minimumVersion = 0,
                        appDeactivated = false,
                        informationURL = "dummy",
                        configTtlSeconds = 0,
                        maxValidityHours = 48
                    )
                ),
                testResultAttributesUseCase = fakeTestResultAttributesUseCase(
                    sampleTimeSeconds = OffsetDateTime.ofInstant(
                        Instant.parse("2021-01-01T00:00:00.00Z"),
                        ZoneId.of("UTC")
                    ).toEpochSecond()
                ),
                personalDetailsUtil = fakePersonalDetailsUtil()
            )

            val localTestResult = usecase.get(LocalTestResultState.None)
            assertTrue((localTestResult as LocalTestResultState.Valid).firstTimeCreated)
            verify(exactly = 0) { mockedPersistenceManager.deleteCredentials() }
        }

    @Test
    fun `Local test result that is expired saved returns Expired and clears persisted credentials`() =
        runBlocking {
            val mockedPersistenceManager: PersistenceManager = mockk(relaxed = true)
            every { mockedPersistenceManager.getCredentials() } answers { "" }

            val usecase = LocalTestResultUseCaseImpl(
                persistenceManager = mockedPersistenceManager,
                testResultUtil = TestResultUtil(
                    clock = Clock.fixed(Instant.parse("2021-01-04T00:00:00.00Z"), ZoneId.of("UTC"))
                ),
                testResultAttributesUseCase = fakeTestResultAttributesUseCase(
                    sampleTimeSeconds = OffsetDateTime.ofInstant(
                        Instant.parse("2021-01-01T00:00:00.00Z"),
                        ZoneId.of("UTC")
                    ).toEpochSecond()
                ),
                cachedAppConfigUseCase = fakeCachedAppConfigUseCase(),
                personalDetailsUtil = fakePersonalDetailsUtil()
            )

            val localTestResult = usecase.get(null)
            assertTrue(localTestResult is LocalTestResultState.Expired)
            verify(exactly = 1) { mockedPersistenceManager.deleteCredentials() }
        }
}
