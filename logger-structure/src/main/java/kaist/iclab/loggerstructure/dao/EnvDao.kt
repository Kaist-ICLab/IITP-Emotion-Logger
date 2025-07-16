package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.summary.EnvSummary
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

    @Query("""
        SELECT 
            (timestamp / 600000) * 600000 AS bucketStart,
            COUNT(*) AS count,
            AVG(temperature) AS avgTemperature,
            AVG(humidity) AS avgHumidity,
            AVG(co2) AS avgCo2,
            AVG(tvoc) AS avgTvoc,
            AVG(tvoc * tvoc) - AVG(tvoc) * AVG(tvoc) AS varTvoc
        FROM envEvent
        WHERE bucketStart >= :timestamp
        GROUP BY bucketStart
        ORDER BY bucketStart
    """)
    fun getSummary(timestamp: Long): List<EnvSummary>
}