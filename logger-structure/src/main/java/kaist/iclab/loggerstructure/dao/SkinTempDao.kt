package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.SkinTempEntity

@Dao
interface SkinTempDao {
    @Query("SELECT * FROM skinTempEvent")
     suspend fun getAll(): List<SkinTempEntity>
    @Insert
     suspend fun insertEvent(skinTempEntity: SkinTempEntity)

    @Insert
     suspend fun insertEvents(skinTempEntities: List<SkinTempEntity>)

    @Query("DELETE FROM skinTempEvent")
     suspend fun deleteAll()

    @Query("SELECT * FROM skinTempEvent ORDER BY timestamp DESC LIMIT 1")
     suspend fun getLast(): SkinTempEntity
}