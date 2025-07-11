package kaist.iclab.wearablelogger.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kaist.iclab.loggerstructure.core.IdRange
import kotlinx.coroutines.tasks.await

class AckRepository(
    context : Context
) {
    companion object {
        private val TAG = AckRepository::class.simpleName
        private const val DATA_PATH = "/WEARABLE_DATA_ACC"
    }

    // TODO: it is sending ACK to its own!
    private val dataClient = Wearable.getDataClient(context)

    suspend fun returnAck(key: String, idRange: IdRange) {
        val request = PutDataMapRequest.create(DATA_PATH).apply {
            dataMap.putString("collectorType", key)
            dataMap.putLong("startId", idRange.startId)
            dataMap.putLong("endId", idRange.endId)
            asPutDataRequest()
        }.asPutDataRequest().setUrgent()
        val result = dataClient.putDataItem(request).await()
        Log.d(TAG, "$key ACK has been sent: $result")
    }
}