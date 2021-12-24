package nl.rijksoverheid.ctr.verifier.persistence.usecase

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.espresso.internal.inject.InstrumentationContext
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.shared.ext.toHex
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.verifier.persistance.usecase.RandomKeyUseCaseImpl
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class RandomKeyUseCaseImplTest: AutoCloseKoinTest() {

    private lateinit var persistenceManager: PersistenceManager

    @Before
    fun setup() {
        this.persistenceManager = SharedPreferencesPersistenceManager(InstrumentationRegistry.getInstrumentation().context.getSharedPreferences(
            "test",
            Context.MODE_PRIVATE
        ))
    }

    @Test
    fun `exists returns true if random key has been persisted`() {
        val androidUtil = mockk<AndroidUtil>()
        every { androidUtil.generateRandomKey() } answers { "123".toByteArray() }

        val usecase = RandomKeyUseCaseImpl(
            persistenceManager = persistenceManager,
            androidUtil = androidUtil
        )

        usecase.persist()

        assertEquals(true, usecase.exists())
    }

    @Test
    fun `exists returns false if random key has not been persisted`() {
        val androidUtil = mockk<AndroidUtil>()
        every { androidUtil.generateRandomKey() } answers { "".toByteArray() }

        val usecase = RandomKeyUseCaseImpl(
            persistenceManager = persistenceManager,
            androidUtil = androidUtil
        )

        assertEquals(false, usecase.exists())
    }

    @Test
    fun `get returns the persisted random key`() {
        val androidUtil = mockk<AndroidUtil>()
        every { androidUtil.generateRandomKey() } answers { "123".toByteArray() }

        val usecase = RandomKeyUseCaseImpl(
            persistenceManager = persistenceManager,
            androidUtil = androidUtil
        )

        usecase.persist()

        assertEquals("123".toByteArray().toHex(), usecase.get())
    }

    @Test
    fun `persist persists the random key`() {
        val persistenceManager = mockk<PersistenceManager>(relaxed = true)
        val androidUtil = mockk<AndroidUtil>()
        every { androidUtil.generateRandomKey() } answers { "123".toByteArray() }

        val usecase = RandomKeyUseCaseImpl(
            persistenceManager = persistenceManager,
            androidUtil = androidUtil
        )

        usecase.persist()

        verify { persistenceManager.saveRandomKey("123".toByteArray().toHex()) }
    }
}