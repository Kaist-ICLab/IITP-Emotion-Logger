package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Query("SELECT * FROM record ORDER BY id DESC")
    fun getAll(): Flow<List<RecordEntity>>

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertEvent(event: RecordEntity)

    @Query("DELETE FROM record WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM record")
    suspend fun deleteAll()
}