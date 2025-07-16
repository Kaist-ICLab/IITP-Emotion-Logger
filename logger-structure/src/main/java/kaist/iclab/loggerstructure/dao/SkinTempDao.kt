package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.summary.SkinTempSummary

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

    @Upsert
    suspend fun upsertEvents(skinTempEntities: List<SkinTempEntity>)

    @Query("DELETE FROM skinTempEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM skinTempEvent WHERE 1")
    suspend fun deleteAll()

    @Query("""
        SELECT 
            (timestamp / 600000) * 600000 AS bucketStart,
            COUNT(*) AS count,
            AVG(objectTemp) AS avgObjectTemp,
            AVG(ambientTemp) AS avgAmbientTemp,
            SUM(CASE WHEN status != 0 THEN 1 ELSE 0 END) AS skinTempBadStatusCount
        FROM skinTempEvent
        WHERE bucketStart >= :timestamp
        GROUP BY bucketStart
        ORDER BY bucketStart
    """)
    fun getSummary(timestamp: Long): List<SkinTempSummary>
}