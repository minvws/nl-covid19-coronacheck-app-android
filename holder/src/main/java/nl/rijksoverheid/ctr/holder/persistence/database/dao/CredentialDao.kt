package nl.rijksoverheid.ctr.holder.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface CredentialDao {

    @Query("SELECT * FROM credential")
    suspend fun getAll(): List<CredentialEntity>

    @Insert
    suspend fun insert(entity: CredentialEntity)

    @Update
    suspend fun update(entity: CredentialEntity)

    @Query("DELETE FROM credential WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM credential")
    suspend fun deleteAll()
}
