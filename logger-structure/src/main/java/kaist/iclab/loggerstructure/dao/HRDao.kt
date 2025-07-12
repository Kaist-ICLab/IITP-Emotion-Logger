package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.HREntity

@Dao
interface HRDao {
    @Query("SELECT * FROM hrEvent WHERE id >= :startId AND id <= :endId ORDER BY id ASC LIMIT :limit")
    suspend fun getChunkBetween(startId: Long, endId: Long, limit: Long): List<HREntity>

    @Query("SELECT * FROM hrEvent")
    suspend fun getAll(): List<HREntity>

    @Query("SELECT id FROM hrEvent ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM hrEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): HREntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(hrEntity: HREntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<HREntity>)

    @Query("DELETE FROM hrEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM hrEvent")
    suspend fun deleteAll()
}