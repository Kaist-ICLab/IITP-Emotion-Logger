package kaist.iclab.loggerstructure.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kaist.iclab.loggerstructure.core.EntityBase

@Entity(tableName= "envEvent")
data class EnvEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val temperature: Double,
    val humidity: Double,
    val co2: Int,
    val tvoc: Int,
): EntityBase

val defaultEnvEntity = EnvEntity(
    id = 0,
    timestamp = -1,
    temperature = 0.0,
    humidity = 0.0,
    co2 = 0,
    tvoc = 0,
)