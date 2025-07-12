package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.SkinTempEntity

@Dao
interface SkinTempDao {
    @Query("SELECT * FROM skinTempEvent WHERE id >= :startId AND id <= :endId ORDER BY id ASC LIMIT :limit")
    suspend fun getChunkBetween(startId: Long, endId: Long, limit: Long): List<SkinTempEntity>

    @Query("SELECT * FROM skinTempEvent")
    suspend fun getAll(): List<SkinTempEntity>

    @Query("SELECT id FROM skinTempEvent ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM skinTempEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): SkinTempEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(skinTempEntity: SkinTempEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<SkinTempEntity>)

    @Query("DELETE FROM skinTempEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM skinTempEvent WHERE 1")
    suspend fun deleteAll()
}