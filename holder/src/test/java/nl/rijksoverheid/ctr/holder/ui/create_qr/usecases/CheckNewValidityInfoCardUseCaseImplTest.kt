/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import java.time.OffsetDateTime
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CheckNewValidityInfoCardUseCaseImplTest: AutoCloseKoinTest() {

    private val holderDatabase: HolderDatabase by inject()
    private val usecase: CheckNewValidityInfoCardUseCase by inject()
    private val persistenceManager: PersistenceManager by inject()

    @Test
    fun `Correct values are set when feature is live and has domestic vaccination green card`() = runBlocking {
        // Insert wallet
        val wallet = WalletEntity(
            id = 1,
            label = "test"
        )
        holderDatabase.walletDao().insert(wallet)

        // Insert domestic vaccination green card
        val greenCard = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOrigin = OriginEntity(
            id = 1,
            greenCardId = 1,
            type = OriginType.Vaccination,
            eventTime = OffsetDateTime.now(),
            expirationTime = OffsetDateTime.now(),
            validFrom = OffsetDateTime.now()
        )

        holderDatabase.greenCardDao().insert(greenCard)
        holderDatabase.originDao().insert(vaccinationOrigin)

        // Enable our feature flag
        loadKoinModules(
            module(override = true) {
                factory<CachedAppConfigUseCase> {
                    object: CachedAppConfigUseCase {
                        override fun getCachedAppConfig(): HolderConfig {
                            return HolderConfig.default(
                                showNewValidityInfoCard = true
                            )
                        }
                    }
                }
            }
        )

        // Execute usecase
        usecase.check()

        // Expected values
        assertFalse(persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity())
        assertFalse(persistenceManager.getHasDismissedNewValidityInfoCard())
    }

    @Test
    fun `Correct values are set when feature is live and has domestic recovery green card`() = runBlocking {
        // Insert wallet
        val wallet = WalletEntity(
            id = 1,
            label = "test"
        )
        holderDatabase.walletDao().insert(wallet)

        // Insert domestic vaccination green card
        val greenCard = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOrigin = OriginEntity(
            id = 1,
            greenCardId = 1,
            type = OriginType.Recovery,
            eventTime = OffsetDateTime.now(),
            expirationTime = OffsetDateTime.now(),
            validFrom = OffsetDateTime.now()
        )

        holderDatabase.greenCardDao().insert(greenCard)
        holderDatabase.originDao().insert(vaccinationOrigin)

        // Enable our feature flag
        loadKoinModules(
            module(override = true) {
                factory<CachedAppConfigUseCase> {
                    object: CachedAppConfigUseCase {
                        override fun getCachedAppConfig(): HolderConfig {
                            return HolderConfig.default(
                                showNewValidityInfoCard = true
                            )
                        }
                    }
                }
            }
        )

        // Execute usecase
        usecase.check()

        // Expected values
        assertFalse(persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity())
        assertFalse(persistenceManager.getHasDismissedNewValidityInfoCard())
    }

    @Test
    fun `Correct values are set when feature is live and has domestic test green card`() = runBlocking {
        // Insert wallet
        val wallet = WalletEntity(
            id = 1,
            label = "test"
        )
        holderDatabase.walletDao().insert(wallet)

        // Insert domestic vaccination green card
        val greenCard = GreenCardEntity(
            id = 1,
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOrigin = OriginEntity(
            id = 1,
            greenCardId = 1,
            type = OriginType.Test,
            eventTime = OffsetDateTime.now(),
            expirationTime = OffsetDateTime.now(),
            validFrom = OffsetDateTime.now()
        )

        holderDatabase.greenCardDao().insert(greenCard)
        holderDatabase.originDao().insert(vaccinationOrigin)

        // Enable our feature flag
        loadKoinModules(
            module(override = true) {
                factory<CachedAppConfigUseCase> {
                    object: CachedAppConfigUseCase {
                        override fun getCachedAppConfig(): HolderConfig {
                            return HolderConfig.default(
                                showNewValidityInfoCard = true
                            )
                        }
                    }
                }
            }
        )

        // Execute usecase
        usecase.check()

        // Expected values
        assertTrue(persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity())
        assertTrue(persistenceManager.getHasDismissedNewValidityInfoCard())
    }

    @Test
    fun `Correct values are set when feature is not live`() = runBlocking {
        // Enable our feature flag
        loadKoinModules(
            module(override = true) {
                factory<CachedAppConfigUseCase> {
                    object: CachedAppConfigUseCase {
                        override fun getCachedAppConfig(): HolderConfig {
                            return HolderConfig.default(
                                showNewValidityInfoCard = false
                            )
                        }
                    }
                }
            }
        )

        // Execute usecase
        usecase.check()

        // Expected values
        assertTrue(persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity())
        assertTrue(persistenceManager.getHasDismissedNewValidityInfoCard())
    }
}