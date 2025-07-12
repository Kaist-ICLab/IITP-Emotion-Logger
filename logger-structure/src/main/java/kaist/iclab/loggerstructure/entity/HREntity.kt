package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(
    tableName = "hrEvent",
    indices = [Index(value = ["timestamp"], unique = true)]
)
data class HREntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val dataReceived: Long,
    val timestamp: Long,
    val hr: Int,
    val hrStatus: Int,
    val ibi: List<Int>,
    val ibiStatus: List<Int>
): EntityBase

val defaultHREntity = HREntity(
    id = 0,
    dataReceived = -1,
    timestamp = -1,
    hr = -1,
    hrStatus = 0,
    ibi = listOf(),
    ibiStatus = listOf()
)