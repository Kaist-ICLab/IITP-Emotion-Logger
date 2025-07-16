package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "ppgEvent",
    indices = [Index(value = ["timestamp"], unique = true)]
)
data class PpgEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val dataReceived: Long,
    val timestamp: Long,
    val ppgGreen : Int,
    val ppgIR: Int,
    val ppgRed: Int,
    val status: Int
): EntityBase

val defaultPpgEntity = PpgEntity(
    id = 0,
    dataReceived = -1,
    timestamp = -1,
    ppgGreen = -1,
    ppgIR = -1,
    ppgRed = -1,
    status = 0
)