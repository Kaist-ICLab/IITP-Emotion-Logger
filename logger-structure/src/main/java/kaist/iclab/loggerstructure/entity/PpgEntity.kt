package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "ppgEvent",
)
data class PpgEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val dataReceived: Long,
    val timestamp: Long,
    val ppg : Int,
    val status: Int
): EntityBase
