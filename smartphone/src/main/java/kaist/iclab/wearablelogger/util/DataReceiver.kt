package kaist.iclab.wearablelogger.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.wearablelogger.dao.RecentDao
import kaist.iclab.wearablelogger.entity.RecentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataReceiver(
    val context: Context,
    val recentDao: RecentDao,
    val collectorDao: Map<String, DaoWrapper<EntityBase>>,
    val dataUploaderRepository: DataUploaderRepository
) : DataClient.OnDataChangedListener {
    companion object {
        private const val TAG = "DataReceiver"
    }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        dataEventBuffer.forEach { dataEvent ->
            val dataType = dataEvent.dataItem.uri.path?.split("/")
            if(dataType == null)
                return

            val data = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
            if(dataType[1] == "WEARABLE"){
                unpackRecentData(data)
            } else {
                unpackDataAsset(data, dataType[2])
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun unpackRecentData(data: DataMap) {
        Log.d(TAG, "received RecentEntity data")
        val entity = RecentEntity(
            timestamp = data.getLong("timestamp"),
            acc = data.getString("acc") ?: "null",
            ppg = data.getString("ppg") ?: "null",
            hr = data.getString("hr") ?: "null",
            skinTemp = data.getString("skin") ?: "null",
        )
        val recentEntities = mutableListOf<RecentEntity>(entity)

        CoroutineScope(Dispatchers.IO).launch {
            recentDao.insertEvents(recentEntities)
            dataUploaderRepository.uploadRecentData()
        }
    }

    private fun unpackDataAsset(data: DataMap, key: String) {
        Log.d(TAG, "unpack($key): $data")
        val asset = data.getAsset("data")!!

        Wearable.getDataClient(context).getFdForAsset(asset)
            .addOnSuccessListener { assetFd ->
                assetFd.inputStream.use { inputStream ->
                    val json = String(inputStream.readBytes())
                    val daoWrapper = collectorDao[key]!!

                    CoroutineScope(Dispatchers.IO).launch {
                        daoWrapper.insertEventsFromJson(json)
                    }
                }
            }
    }
}