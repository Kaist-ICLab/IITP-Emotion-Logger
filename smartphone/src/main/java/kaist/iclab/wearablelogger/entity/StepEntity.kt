package kaist.iclab.wearablelogger.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "stepEvent",
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val dataReceived: Long,
    val startTime: Long,
    val endTime: Long,
    val step: Long,
): EntityBase