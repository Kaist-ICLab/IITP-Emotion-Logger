package kaist.iclab.wearablelogger.uploader

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kaist.iclab.loggerstructure.core.CollectorInterface
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

private const val TAG = "UploaderRepository"

class UploaderRepository(
    private val androidContext: Context,
) {
    private val dataPath = "/WEARABLE_DATA"
    fun upload2Phone(collector: CollectorInterface) {
        Log.d(TAG, "sendData2Phone")
        val dataClient = Wearable.getDataClient(androidContext)

        // Try one at a time
        runBlocking {
            try {
                val pair = collector.stringifyData()
                val stringData = pair.first
                val lastTimestamp = pair.second

                // 1.5h worth of SQL is very likely to be >100KB
                // So convert to Asset
                Log.d(TAG, "${collector.key} data: $stringData")
                val asset = Asset.createFromBytes(stringData.toByteArray())

                // Unique dataPath for each collector for individual robustness
                val request = PutDataMapRequest.create("$dataPath/${collector.key}/${System.currentTimeMillis()}").run {
                    dataMap.putAsset("data", asset)
                    asPutDataRequest()
                }

                dataClient.putDataItem(request).await()
                Log.d(TAG, "${collector.key} Data has been uploaded")
                collector.flushBefore(lastTimestamp)

            } catch (exception: Exception) {
                Log.e(TAG, "Saving DataItem failed: $exception")
            }
        }
    }
}