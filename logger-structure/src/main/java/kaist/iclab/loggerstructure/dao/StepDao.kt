package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.StepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM stepEvent")
    suspend fun getAll(): List<StepEntity>
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(stepEntity: StepEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(stepEntity: List<StepEntity>)

    @Query("DELETE FROM stepEvent")
    suspend fun deleteAll()

    @Query("SELECT * FROM stepEvent ORDER BY endTime DESC LIMIT 1")
    suspend fun getLast(): StepEntity?

    @Query("SELECT * FROM stepEvent ORDER BY endTime DESC LIMIT 1")
    fun getLastByFlow(): Flow<StepEntity?>
}