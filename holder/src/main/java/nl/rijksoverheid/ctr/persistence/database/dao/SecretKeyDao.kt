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
import nl.rijksoverheid.ctr.persistence.database.entities.SecretKeyEntity

@Dao
interface SecretKeyDao {
    @Query("SELECT * FROM secret_key WHERE green_card_id = :greenCardId")
    suspend fun get(greenCardId: Int): SecretKeyEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SecretKeyEntity)
}