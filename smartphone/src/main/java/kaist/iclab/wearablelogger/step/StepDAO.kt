package kaist.iclab.wearablelogger.step

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface StepDAO {
    @Query("SELECT * FROM stepEvent")
    suspend fun getAll(): List<StepEntity>
    @Insert
    suspend fun insertStepEvent(stepEntity: StepEntity)
    @Insert
    suspend fun insertStepEvents(stepEntity: List<StepEntity>)

    @Query("DELETE FROM stepEvent")
    suspend fun deleteAll()

    @Query("SELECT * FROM stepEvent ORDER BY endTime DESC LIMIT 1")
    suspend fun getLast(): StepEntity
}