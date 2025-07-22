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

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(accEntity: AccEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<AccEntity>)

    @Query("DELETE FROM accEvent WHERE dataReceived >= :startTime AND dataReceived <= :endTime")
    suspend fun deleteBetween(startTime: Long, endTime: Long)

    @Query("DELETE FROM accEvent")
    suspend fun deleteAll()
}