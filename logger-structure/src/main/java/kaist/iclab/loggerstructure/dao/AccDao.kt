package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.AccEntity

@Dao
interface AccDao {
    @Query("SELECT * FROM accEvent")
    suspend fun getAll(): List<AccEntity>

    @Insert
    suspend fun insertEvent(accEntity: AccEntity)

    @Insert
    suspend fun insertEvents(accEntities: List<AccEntity>)

    @Query("DELETE FROM accEvent")
    suspend fun deleteAll()

    @Query("SELECT * FROM accEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): AccEntity?
}