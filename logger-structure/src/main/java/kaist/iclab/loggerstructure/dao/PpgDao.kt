package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.summary.PpgSummary

@Dao
interface PpgDao {
    @Query("SELECT * FROM ppgEvent WHERE id >= :startId AND id <= :endId ORDER BY id ASC LIMIT :limit")
    suspend fun getChunkBetween(startId: Long, endId: Long, limit: Long): List<PpgEntity>

    @Query("SELECT * FROM ppgEvent")
    suspend fun getAll(): List<PpgEntity>

    @Query("SELECT id FROM ppgEvent ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM ppgEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): PpgEntity?

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(ppgEntity: PpgEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<PpgEntity>)

    @Upsert
    suspend fun upsertEvents(ppgEntities: List<PpgEntity>)

    @Query("DELETE FROM ppgEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM ppgEvent")
    suspend fun deleteAll()

    @Query("""
        SELECT 
            (timestamp / 600000) * 600000 AS bucketStart,
            COUNT(*) AS count,
            AVG(ppgGreen) AS avgPpgGreen,
            AVG(ppgIR) AS avgPpgIR,
            AVG(ppgRed) AS avgPpgRed,
            AVG(ppgRed * ppgRed) - AVG(ppgRed) * AVG(ppgRed) AS varPpgRed,
            SUM(CASE WHEN status != 0 THEN 1 ELSE 0 END) AS badStatusCount
        FROM ppgEvent
        WHERE bucketStart >= :timestamp
        GROUP BY bucketStart
        ORDER BY bucketStart
    """)
    fun getSummary(timestamp: Long): List<PpgSummary>
}