package kaist.iclab.wearablelogger.uploader

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.loggerstructure.core.CollectorInterface
import kaist.iclab.loggerstructure.util.DataClientPath
import kaist.iclab.wearablelogger.MyDataRoomDB
import kaist.iclab.wearablelogger.collector.core.UploadAlarmReceiver
import kotlinx.coroutines.tasks.await
import java.lang.Thread.sleep


class UploaderRepository(
    private val context: Context,
    private val db: MyDataRoomDB,
) {
    companion object {
        private const val TAG = "UploaderRepository"
        private const val CHUNK_SIZE = 5000L
    }
    private val dataClient by lazy { Wearable.getDataClient(context) }

    suspend fun uploadFullData(collector: CollectorInterface) {
        Log.d(TAG, "sendData2Phone")
        val dataClient = Wearable.getDataClient(context)

        try {
            // Try one at a time
            var chunkIndex = 0
            for(stringData in collector.getBeforeLast(0, CHUNK_SIZE)) {
                // 15min worth of accelerometer & PPG SQL is very likely to be >100KB
                // So convert to Asset
                Log.d(TAG, "${collector.key} data: $stringData")
                val asset = Asset.createFromBytes(stringData.toByteArray())

                // Unique dataPath for each collector for individual robustness
                val request = PutDataMapRequest.create("${DataClientPath.UPLOAD_DATA}/${collector.key}/${System.currentTimeMillis()}").run {
                    dataMap.putAsset("data", asset)
                    asPutDataRequest()
                }

                dataClient.putDataItem(request).await()
                Log.d(TAG, "${collector.key} Data ($chunkIndex) has been uploaded")
                chunkIndex++
                sleep(2000)
            }

        } catch (exception: Exception) {
            Log.e(TAG, "Saving DataItem failed: $exception")
        }
    }

    suspend fun uploadRecentData() {
        val request = PutDataMapRequest.create("/WEARABLE").apply {
            dataMap.putLong("timestamp", System.currentTimeMillis())
            dataMap.putString("acc", Gson().toJson(db.accDao().getLast()))
            dataMap.putString("hr", Gson().toJson(db.hrDao().getLast()))
            dataMap.putString("ppg", Gson().toJson(db.ppgDao().getLast()))
            dataMap.putString("skin", Gson().toJson(db.skinTempDao().getLast()))
            dataMap.putLong("watch_upload_schedule", AlarmScheduler.nextAlarmSchedule.value[UploadAlarmReceiver::class.simpleName] ?: 0)
        }.asPutDataRequest().setUrgent()

        val result = dataClient.putDataItem(request).await()
        Log.d(TAG, "COLLECTOR SEND $result")
    }
}