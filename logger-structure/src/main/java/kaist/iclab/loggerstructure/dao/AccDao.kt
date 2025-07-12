package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.AccEntity

@Dao
interface AccDao {
    @Query("SELECT * FROM accEvent WHERE id >= :startId AND id <= :endId ORDER BY id ASC LIMIT :limit")
    suspend fun getChunkBetween(startId: Long, endId: Long, limit: Long): List<AccEntity>

    @Query("SELECT * FROM accEvent")
    suspend fun getAll(): List<AccEntity>

    @Query("SELECT id FROM accEvent ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM accEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): AccEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(accEntity: AccEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<AccEntity>)

    @Query("DELETE FROM accEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM accEvent")
    suspend fun deleteAll()
}