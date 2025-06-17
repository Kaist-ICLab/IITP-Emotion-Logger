package kaist.iclab.loggerstructure.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kaist.iclab.loggerstructure.entity.TestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
     suspend fun insertEvent(testEntity: TestEntity)

    @Query("SELECT * FROM testEvent WHERE timestamp > :timestamp")
    fun queryTestEvent(timestamp: Long): Flow<List<TestEntity>>

    @Query("SELECT * FROM testEvent")
     suspend fun getAll(): List<TestEntity>
}