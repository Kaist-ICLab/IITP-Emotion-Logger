package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accEvent",
    indices = [Index(value = ["timestamp"], unique = true)]
)
data class AccEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val pid: String,
    val label: String,
    val timestamp: Long,
    val dataReceived: Long,
    val x : Float,
    val y : Float,
    val z : Float,
)
//
//val defaultAccEntity = AccEntity(
//    id = 0,
//    pid = "",
//    label = "",
//    timestamp = -1,
//    x = 0.0F,
//    y = 0.0F,
//    z = 0.0F
//)
