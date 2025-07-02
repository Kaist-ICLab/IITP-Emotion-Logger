package kaist.iclab.wearablelogger.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.wearablelogger.entity.EnvEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvDao {
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertEvents(envEntity: List<EnvEntity>)

    @Insert
    suspend fun insertEvent(envEntity: EnvEntity)

    @Query("SELECT * FROM envEvent ORDER BY timestamp DESC LIMIT 1")
    fun getLastByFlow(): Flow<EnvEntity?>

    @Query("SELECT * FROM envEvent ORDER BY timestamp DESC LIMIT 1")
    fun getLast(): EnvEntity?

    @Query("DELETE FROM envEvent")
    fun deleteAll()
}