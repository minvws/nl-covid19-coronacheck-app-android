package nl.rijksoverheid.ctr.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.persistence.database.models.Wallet

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface WalletDao {

    @Transaction
    @Query("SELECT * FROM wallet")
    fun get(): Flow<List<Wallet>>

    @Query("SELECT * FROM wallet")
    suspend fun getAll(): List<WalletEntity>

    @Insert
    suspend fun insert(entity: WalletEntity)

    @Query("DELETE FROM wallet WHERE id = :id")
    suspend fun delete(id: Int)
}
