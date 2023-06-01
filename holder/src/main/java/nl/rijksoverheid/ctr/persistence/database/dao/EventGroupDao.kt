package nl.rijksoverheid.ctr.persistence.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

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

    @Query("SELECT * FROM event_group WHERE id IN (:ids)")
    suspend fun getAllOfIds(ids: List<Int>): List<EventGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entity: List<EventGroupEntity>)

    @Query("UPDATE event_group SET expiryDate = :expiryDate WHERE id = :eventGroupId")
    suspend fun updateExpiryDate(eventGroupId: Int, expiryDate: OffsetDateTime)

    @Query("UPDATE event_group SET draft = :draft")
    suspend fun updateDraft(draft: Boolean)

    @Query("UPDATE event_group SET draft = :draft WHERE id IN (:ids)")
    suspend fun updateDraft(ids: List<Int>, draft: Boolean)

    @Delete
    suspend fun delete(entity: EventGroupEntity)

    @Query("DELETE FROM event_group")
    suspend fun deleteAll()

    @Query("DELETE FROM event_group WHERE type = :originType")
    suspend fun deleteAllOfType(originType: OriginType)

    @Query("DELETE FROM event_group WHERE type NOT IN (:originTypes)")
    suspend fun deleteAllOfNotTypes(originTypes: List<OriginType>)

    @Query("DELETE FROM event_group WHERE id IN (:ids)")
    suspend fun deleteAllOfIds(ids: List<Int>)

    @Query("DELETE FROM event_group WHERE draft = 1")
    suspend fun deleteDraftEvents()
}
