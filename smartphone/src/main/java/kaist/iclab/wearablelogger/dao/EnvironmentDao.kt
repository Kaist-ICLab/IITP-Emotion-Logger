package kaist.iclab.wearablelogger.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.wearablelogger.entity.EnvironmentEntity
import kaist.iclab.wearablelogger.entity.RecentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentDao {
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertEvents(environmentEntity: List<EnvironmentEntity>)

    @Insert
    suspend fun insertEvent(environmentEntity: EnvironmentEntity)

    @Query("SELECT * FROM environmentEvent ORDER BY timestamp DESC LIMIT 1")
    fun getLastEvent(): Flow<EnvironmentEntity?>

    @Query("DELETE FROM environmentEvent")
    fun deleteAll()
}