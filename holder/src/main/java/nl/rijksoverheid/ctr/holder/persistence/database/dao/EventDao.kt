package nl.rijksoverheid.ctr.holder.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface EventDao {

    @Query("SELECT * FROM event")
    suspend fun getAll(): List<EventEntity>

    @Insert
    suspend fun insert(entity: EventEntity)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun delete(id: Int)
}
