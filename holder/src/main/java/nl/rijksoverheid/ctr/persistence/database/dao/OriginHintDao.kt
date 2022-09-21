package nl.rijksoverheid.ctr.persistence.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import nl.rijksoverheid.ctr.persistence.database.entities.OriginHintEntity

@Dao
interface OriginHintDao {
    @Query("SELECT * FROM origin_hint WHERE hint = :hint")
    suspend fun get(hint: String): List<OriginHintEntity>

    @Query("DELETE FROM origin_hint")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OriginHintEntity)
}
