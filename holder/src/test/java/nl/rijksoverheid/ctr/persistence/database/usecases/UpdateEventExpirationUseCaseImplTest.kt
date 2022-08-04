/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.persistence.database.usecases

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UpdateEventExpirationUseCaseImplTest : AutoCloseKoinTest() {

    private lateinit var db: HolderDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HolderDatabase::class.java).build()
    }

    @Test
    fun `Update event group in database when expire date is known`() = runBlocking {
        // Insert database models
        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "owner"
            )
        )

        db.eventGroupDao().insertAll(
            listOf(
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "",
                    type = OriginType.Test,
                    scope = "",
                    expiryDate = null,
                    jsonData = "".toByteArray()
                )
            )
        )

        // Execute usecase
        val eventGroupExpireDate = OffsetDateTime.ofInstant(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val usecase = UpdateEventExpirationUseCaseImpl(
            holderDatabase = db
        )

        usecase.update(
            blobExpireDates = listOf(
                RemoteGreenCards.BlobExpiry(
                    id = 1,
                    expiry = eventGroupExpireDate
                )
            )
        )

        // The event group should have a expiration date set
        val eventGroup = db.eventGroupDao().getAll().first()
        assertEquals(eventGroupExpireDate, eventGroup.expiryDate)
    }

    @Test
    fun `Update event group in database does not crash if event cannot be found`() = runBlocking {
        // Insert database models
        db.walletDao().insert(
            WalletEntity(
                id = 1,
                label = "owner"
            )
        )

        db.eventGroupDao().insertAll(
            listOf(
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "",
                    type = OriginType.Test,
                    scope = "",
                    expiryDate = null,
                    jsonData = "".toByteArray()
                )
            )
        )

        // Execute usecase with a event id that cannot be found
        val eventGroupExpireDate = OffsetDateTime.ofInstant(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val usecase = UpdateEventExpirationUseCaseImpl(
            holderDatabase = db
        )

        usecase.update(
            blobExpireDates = listOf(
                RemoteGreenCards.BlobExpiry(
                    id = 2,
                    expiry = eventGroupExpireDate
                )
            )
        )

        // The event group should have a expiration date set
        val eventGroup = db.eventGroupDao().getAll().first()
        assertEquals(null, eventGroup.expiryDate)
    }
}
