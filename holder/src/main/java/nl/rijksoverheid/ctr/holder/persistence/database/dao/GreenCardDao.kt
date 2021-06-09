package nl.rijksoverheid.ctr.holder.persistence.database.dao

import androidx.room.*
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard

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
    suspend fun getAll(): List<GreenCard>

    @Query("SELECT * FROM green_card WHERE type = :type AND wallet_id = :walletId")
    suspend fun getAll(type: GreenCardType, walletId: Int): List<GreenCard>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GreenCardEntity): Long

    @Update
    suspend fun update(entity: GreenCardEntity)

    @Delete
    suspend fun delete(entity: GreenCardEntity)

    @Query("DELETE FROM green_card")
    suspend fun deleteAll()
}
