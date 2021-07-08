package nl.rijksoverheid.ctr.holder.persistence.database.dao

import androidx.room.*
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface EventGroupDao {

    @Query("SELECT * FROM event_group")
    suspend fun getAll(): List<EventGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entity: List<EventGroupEntity>)

    @Delete
    suspend fun delete(entity: EventGroupEntity)

    @Query("DELETE FROM event_group")
    suspend fun deleteAll()

    @Query("DELETE FROM event_group WHERE type = :originType")
    suspend fun deleteAllOfType(originType: OriginType)
}
