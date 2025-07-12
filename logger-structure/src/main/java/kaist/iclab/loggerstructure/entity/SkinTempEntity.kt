package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "skinTempEvent",
    indices = [Index(value = ["timestamp"], unique = true)]
)
data class SkinTempEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val dataReceived: Long,
    val timestamp: Long,
    val ambientTemp: Float,
    val objectTemp: Float,
    val status: Int
): EntityBase

val defaultSkinTempEntity = SkinTempEntity(
    id = 0,
    dataReceived = -1,
    timestamp = -1,
    ambientTemp = 0.0F,
    objectTemp = 0.0F,
    status = 0,
)
