package kaist.iclab.loggerstructure.summary

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PpgSummary(
    @SerializedName("bucket_start")
    val bucketStart: Long,

    @SerializedName("count")
    val count: Int,

    @SerializedName("avg_ppg_green")
    val avgPpgGreen: Double,

    @SerializedName("avg_ppg_ir")
    val avgPpgIR: Double,

    @SerializedName("avg_ppg_red")
    val avgPpgRed: Double,

    @SerializedName("ppg_bad_status_count")
    val ppgBadStatusCount: Int
) : Serializable