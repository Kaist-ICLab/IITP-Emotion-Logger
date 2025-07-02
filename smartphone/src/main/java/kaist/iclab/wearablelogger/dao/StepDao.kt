package kaist.iclab.wearablelogger.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kaist.iclab.wearablelogger.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM stepEvent")
    suspend fun getAll(): List<StepEntity>
    @Insert
    suspend fun insertEvent(stepEntity: StepEntity)
    @Insert
    suspend fun insertEvents(stepEntity: List<StepEntity>)

    @Query("SELECT * FROM stepEvent ORDER BY endTime DESC LIMIT 1")
    suspend fun getLast(): StepEntity?

    @Query("SELECT * FROM stepEvent ORDER BY endTime DESC LIMIT 1")
    fun getLastByFlow(): Flow<StepEntity?>

    @Query("DELETE FROM stepEvent")
    suspend fun deleteAll()
}