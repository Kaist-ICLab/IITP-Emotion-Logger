package kaist.iclab.wearablelogger

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.wearablelogger.db.RecentDao
import kaist.iclab.wearablelogger.db.RecentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "DataReceiver"

class DataReceiver(
    val context: Context,
    val recentDao: RecentDao,
    val collectorDao: Map<String, DaoWrapper<EntityBase>>
) : DataClient.OnDataChangedListener {

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        val recentEntities = mutableListOf<RecentEntity>()

        dataEventBuffer.forEach { dataEvent ->
            val dataType = dataEvent.dataItem.uri.path
            if(dataType == null)
                return

            val data = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
            if(dataType == "/WEARABLE"){
                recentEntities.add(
                    RecentEntity(
                        timestamp = data.getLong("timestamp"),
                        acc = data.getString("acc")?:"null",
                        ppg = data.getString("ppg")?:"null",
                        hr = data.getString("hr")?:"null"
                    )
                )
            } else {
                unpackAsset(data)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            recentDao.insertEvents(recentEntities)
        }
    }

    private fun unpackAsset(data: DataMap) {
        Log.v(TAG, "unpack: $data")
        val key = data.getString("key")!!
        val asset = data.getAsset(key)!!

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