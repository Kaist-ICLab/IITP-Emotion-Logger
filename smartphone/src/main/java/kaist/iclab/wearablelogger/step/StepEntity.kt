package kaist.iclab.wearablelogger.step

import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
