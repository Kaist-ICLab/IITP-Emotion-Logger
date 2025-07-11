package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.EnvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvDao {
    @Query("SELECT * FROM envEvent WHERE id >= :startId AND id <= :endId ORDER BY id ASC LIMIT :limit")
    suspend fun getChunkBetween(startId: Long, endId: Long, limit: Long): List<EnvEntity>

    @Query("SELECT * FROM envEvent")
    suspend fun getAll(): List<EnvEntity>

    @Query("SELECT id FROM envEvent ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM envEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): EnvEntity?

    @Query("SELECT * FROM envEvent ORDER BY timestamp DESC LIMIT 1")
    fun getLastByFlow(): Flow<EnvEntity?>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(envEntity: EnvEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<EnvEntity>)

    @Query("DELETE FROM envEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM envEvent")
    suspend fun deleteAll()
}