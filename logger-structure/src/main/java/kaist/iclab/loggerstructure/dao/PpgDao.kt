package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.PpgEntity

@Dao
interface PpgDao {
    @Query("SELECT * FROM ppgEvent")
    suspend fun getAll(): List<PpgEntity>
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(ppgEntity: PpgEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvents(ppgEntities: List<PpgEntity>)

    @Query("DELETE FROM ppgEvent WHERE 1")
    suspend fun deleteAll()

    @Query("SELECT * FROM ppgEvent ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): PpgEntity?
}