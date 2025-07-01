package kaist.iclab.wearablelogger.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.wearablelogger.entity.RecentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentDao {

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertEvents(recentEntity: List<RecentEntity>)

    @Query("SELECT * FROM recent ORDER BY timestamp DESC LIMIT 1")
    fun getLastEvent(): Flow<RecentEntity?>

    @Query("DELETE FROM recent")
    fun deleteAll()
}