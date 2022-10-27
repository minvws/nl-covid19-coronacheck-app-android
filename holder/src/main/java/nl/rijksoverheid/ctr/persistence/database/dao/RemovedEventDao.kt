/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason

@Dao
interface RemovedEventDao {
    @Transaction
    @Query("SELECT * FROM removed_event WHERE reason = :reason")
    suspend fun getAll(reason: RemovedEventReason): List<RemovedEventEntity>

    @Query("DELETE FROM removed_event WHERE reason = :reason")
    suspend fun deleteAll(reason: RemovedEventReason)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RemovedEventEntity)
}
