package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kaist.iclab.loggerstructure.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM stepEvent WHERE dataReceived <= :dataReceived ORDER BY dataReceived ASC")
    suspend fun getBefore(dataReceived: Long): List<StepEntity>

    @Query("SELECT * FROM stepEvent WHERE dataReceived <= :dataReceived ORDER BY dataReceived ASC LIMIT :limit")
    suspend fun getChunkBefore(dataReceived: Long, limit: Int): List<StepEntity>

    @Query("SELECT * FROM stepEvent")
    suspend fun getAll(): List<StepEntity>

    @Insert
    suspend fun insertEvent(stepEntity: StepEntity)

    @Insert
    suspend fun insertEvents(stepEntity: List<StepEntity>)

    @Upsert
    suspend fun upsertEvent(stepEntity: StepEntity)

    @Query("SELECT * FROM stepEvent ORDER BY dataReceived DESC LIMIT 1")
    suspend fun getLast(): StepEntity?

    @Query("SELECT * FROM stepEvent ORDER BY dataReceived DESC LIMIT 1")
    fun getLastByFlow(): Flow<StepEntity?>

    @Query("DELETE FROM stepEvent WHERE dataReceived <= :dataReceived")
    suspend fun deleteBefore(dataReceived: Long)

    @Query("DELETE FROM stepEvent")
    suspend fun deleteAll()
}