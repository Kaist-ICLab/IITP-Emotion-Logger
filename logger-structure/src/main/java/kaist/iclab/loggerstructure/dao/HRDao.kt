package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.summary.HRSummary

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

    @Upsert
    suspend fun upsertEvents(hrEntities: List<HREntity>)

    @Query("DELETE FROM hrEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM hrEvent")
    suspend fun deleteAll()

    @Query("""
        SELECT 
            (timestamp / 600000) * 600000 AS bucketStart,
            COUNT(*) AS count,
            AVG(hr) AS avg,
            AVG(hr * hr) - AVG(hr) * AVG(hr) AS variance,
            SUM(CASE WHEN hrStatus != 1 THEN 1 ELSE 0 END) AS badStatusCount
        FROM hrEvent
        WHERE bucketStart >= :timestamp
        GROUP BY bucketStart
        ORDER BY bucketStart
    """)
    fun getSummary(timestamp: Long): List<HRSummary>
}