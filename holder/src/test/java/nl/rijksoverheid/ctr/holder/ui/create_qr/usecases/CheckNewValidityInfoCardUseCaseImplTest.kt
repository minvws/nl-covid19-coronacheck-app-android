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
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class CheckNewValidityInfoCardUseCaseImplTest: AutoCloseKoinTest() {

    private val usecase: CheckNewValidityInfoCardUseCase by inject()
    private val persistenceManager: PersistenceManager by inject()

    @Test
    fun `Correct values are set when feature is live`() = runBlocking {
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