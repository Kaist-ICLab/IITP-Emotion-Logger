package kaist.iclab.loggerstructure.summary

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HRSummary(
    @SerializedName("bucket_start")
    val bucketStart: Long,

    @SerializedName("count")
    val count: Int,

    @SerializedName("avg_hr")
    val avgHR: Double,

    @SerializedName("hr_bad_status_count")
    val hrBadStatusCount: Int
) : Serializable