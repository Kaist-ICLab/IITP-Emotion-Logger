package kaist.iclab.wearablelogger.uploader

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class AckReceiverService: WearableListenerService() {
    companion object {
        private val TAG = AckReceiverService::class.simpleName
    }

    private val collectors by inject<List<HealthTrackerCollector>>(qualifier = named("collectors"))

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        dataEventBuffer.forEach { dataEvent ->
            val path = dataEvent.dataItem.uri.path
            Log.d(TAG, "Received dataEvent: $path")

            if(path == null) return@forEach
            if(path != "/WEARABLE_DATA_ACK") return@forEach

            val data = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap

            val collectorType = data.getString("collectorType") ?: ""
            val startId = data.getLong("startId")
            val endId = data.getLong("endId")

            if(collectorType == "") return@forEach

            collectors.firstOrNull { it -> it.key == collectorType }?.deleteBetween(startId, endId)
        }
    }
}