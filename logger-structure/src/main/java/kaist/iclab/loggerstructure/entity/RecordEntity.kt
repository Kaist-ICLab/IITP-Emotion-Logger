package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "record",
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 고유 ID
    val label: String,
    val startTime: Long,
    val endTime: Long,
)