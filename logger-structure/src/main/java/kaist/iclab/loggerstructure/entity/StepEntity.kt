package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "stepEvent",
    indices = [Index(value = ["startTime", "endTime"], unique = true)]
)
data class StepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val dataReceived: Long,
    val startTime: Long,
    val endTime: Long,
    val step: Long,
): EntityBase

val defaultStepEntity = StepEntity(
    id = 0,
    dataReceived = -1,
    startTime = -1,
    endTime = -1,
    step = 0
)