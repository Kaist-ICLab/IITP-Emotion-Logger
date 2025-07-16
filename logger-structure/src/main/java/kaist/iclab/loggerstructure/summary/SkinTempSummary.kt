package kaist.iclab.loggerstructure.summary

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SkinTempSummary(
    @SerializedName("bucket_start")
    val bucketStart: Long,

    @SerializedName("count")
    val count: Int,

    @SerializedName("avg_object_temp")
    val avgObjectTemp: Double,

    @SerializedName("avg_ambient_temp")
    val avgAmbientTemp: Double,

    @SerializedName("skintemp_bad_status_count")
    val skinTempBadStatusCount: Int
) : Serializable