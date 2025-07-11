package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kaist.iclab.loggerstructure.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM stepEvent WHERE id >= :startId AND id <= :endId ORDER BY id ASC LIMIT :limit")
    suspend fun getChunkBetween(startId: Long, endId: Long, limit: Long): List<StepEntity>

    @Query("SELECT * FROM stepEvent")
    suspend fun getAll(): List<StepEntity>

    @Query("SELECT id FROM stepEvent ORDER BY id DESC LIMIT 1")
    suspend fun getLastId(): Long?

    @Query("SELECT * FROM stepEvent ORDER BY startTime DESC LIMIT 1")
    suspend fun getLast(): StepEntity?

    @Query("SELECT * FROM stepEvent ORDER BY startTime DESC LIMIT 1")
    fun getLastByFlow(): Flow<StepEntity?>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(stepEntity: StepEntity)

    @Upsert
    suspend fun upsertEvent(stepEntity: StepEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<StepEntity>)

    @Query("DELETE FROM stepEvent WHERE id >= :startId AND id <= :endId")
    suspend fun deleteBetween(startId: Long, endId: Long)

    @Query("DELETE FROM stepEvent WHERE 1")
    suspend fun deleteAll()
}