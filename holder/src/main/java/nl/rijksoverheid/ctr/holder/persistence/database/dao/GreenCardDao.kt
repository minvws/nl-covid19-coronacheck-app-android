package nl.rijksoverheid.ctr.holder.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface GreenCardDao {

    @Query("SELECT * FROM green_card")
    suspend fun getAll(): List<GreenCardEntity>

    @Insert
    suspend fun insert(entity: GreenCardEntity): Long

    @Update
    suspend fun update(entity: GreenCardEntity)

    @Query("DELETE FROM green_card WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM green_card")
    suspend fun deleteAll()
}
