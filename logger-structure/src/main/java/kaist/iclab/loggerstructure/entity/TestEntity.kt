package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "testEvent"
)
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
): EntityBase
