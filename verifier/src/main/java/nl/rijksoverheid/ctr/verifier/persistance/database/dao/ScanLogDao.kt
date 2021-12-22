package nl.rijksoverheid.ctr.verifier.persistance.database.dao

import androidx.room.*
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Dao
interface ScanLogDao {

    @Query("SELECT * FROM scan_log")
    suspend fun getAll(): List<ScanLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ScanLogEntity)

    @Delete
    suspend fun delete(entities: List<ScanLogEntity>)
}
