package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.HREntity

@Dao
interface HRDao {
    @Query("SELECT * FROM hrEvent")
     suspend fun getAll(): List<HREntity>
    @Insert
     suspend fun insertEvent(hrEntity: HREntity)
    @Insert
     suspend fun insertEvents(hrEntities: List<HREntity>)
    @Query("DELETE FROM hrEvent")
     suspend fun deleteAll()

    @Query("SELECT * FROM hrEvent ORDER BY timestamp DESC LIMIT 1")
     suspend fun getLast(): HREntity
}