package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.EnvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvDao {
    @Query("SELECT * FROM envEvent WHERE timestamp <= :timestamp ORDER BY timestamp ASC")
    suspend fun getBefore(timestamp: Long): List<EnvEntity>

    @Query("SELECT * FROM envEvent WHERE timestamp <= :timestamp ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getChunkBefore(timestamp: Long, limit: Int): List<EnvEntity>

    @Query("SELECT * FROM envEvent")
    suspend fun getAll(): List<EnvEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertEvents(envEntity: List<EnvEntity>)

    @Insert
    suspend fun insertEvent(envEntity: EnvEntity)

    @Query("SELECT * FROM envEvent ORDER BY timestamp DESC LIMIT 1")
    fun getLastByFlow(): Flow<EnvEntity?>

    @Query("SELECT * FROM envEvent ORDER BY timestamp DESC LIMIT 1")
    fun getLast(): EnvEntity?

    @Query("DELETE FROM envEvent WHERE timestamp <= :timestamp")
    suspend fun deleteBefore(timestamp: Long)

    @Query("DELETE FROM envEvent")
    fun deleteAll()
}