package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.AccEntity

@Dao
interface AccDao {
    @Query("SELECT * FROM accEvent WHERE timestamp <= :timestamp")
    suspend fun getBefore(timestamp: Long): List<AccEntity>

    @Query("SELECT * FROM accEvent")
    suspend fun getAll(): List<AccEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(accEntity: AccEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(accEntities: List<AccEntity>)

    @Query("DELETE FROM accEvent WHERE timestamp <= :timestamp")
    suspend fun deleteBefore(timestamp: Long)

    @Query("DELETE FROM accEvent WHERE 1")
    suspend fun deleteAll()

    @Query("SELECT * FROM accEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): AccEntity?
}