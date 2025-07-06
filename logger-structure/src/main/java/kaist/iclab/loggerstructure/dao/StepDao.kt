package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kaist.iclab.loggerstructure.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM stepEvent WHERE id <= :id")
    suspend fun getBefore(id: Long): List<StepEntity>

    @Query("SELECT * FROM stepEvent")
    suspend fun getAll(): List<StepEntity>

    @Insert
    suspend fun insertEvent(stepEntity: StepEntity)

    @Insert
    suspend fun insertEvents(stepEntity: List<StepEntity>)

    @Upsert
    suspend fun upsertEvent(stepEntity: StepEntity)

    @Query("SELECT * FROM stepEvent ORDER BY startTime DESC LIMIT 1")
    suspend fun getLast(): StepEntity?

    @Query("SELECT * FROM stepEvent ORDER BY startTime DESC LIMIT 1")
    fun getLastByFlow(): Flow<StepEntity?>

    @Query("DELETE FROM stepEvent WHERE startTime <= :startTime")
    suspend fun deleteBefore(startTime: Long)

    @Query("DELETE FROM stepEvent")
    suspend fun deleteAll()
}