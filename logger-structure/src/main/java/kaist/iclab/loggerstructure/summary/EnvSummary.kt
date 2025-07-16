package kaist.iclab.loggerstructure.summary

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EnvSummary(
    @SerializedName("bucket_start")
    val bucketStart: Long,

    @SerializedName("count")
    val count: Int,

    @SerializedName("avg_temperature")
    val avgTemperature: Double,

    @SerializedName("avg_humidity")
    val avgHumidity: Double,

    @SerializedName("avg_co2")
    val avgCo2: Double,

    @SerializedName("avg_tvoc")
    val avgTvoc: Double,

    @SerializedName("var_tvoc")
    val varTvoc: Double
) : Serializable