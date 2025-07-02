package kaist.iclab.wearablelogger.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName= "envEvent")
data class EnvEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val temperature: Double,
    val humidity: Double,
    val co2: Int,
    val tvoc: Int,
)