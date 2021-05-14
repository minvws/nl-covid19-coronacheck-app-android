package nl.rijksoverheid.ctr.holder.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import nl.rijksoverheid.ctr.holder.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.holder.persistence.database.models.Wallet

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface WalletDao {

    @Query("SELECT * FROM wallet")
    fun get(): Flow<List<Wallet>>

    @Insert
    suspend fun insert(entity: WalletEntity)

    @Query("DELETE FROM wallet WHERE id = :id")
    suspend fun delete(id: Int)
}
