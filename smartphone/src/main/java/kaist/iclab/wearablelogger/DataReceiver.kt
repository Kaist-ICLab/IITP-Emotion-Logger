package kaist.iclab.wearablelogger

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
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
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

private const val TAG = "DataReceiver"

class DataReceiver(
    val context: Context,
    val recentDao: RecentDao,
    val collectorDao: Map<String, DaoWrapper<EntityBase>>
) : DataClient.OnDataChangedListener {

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
            acc = data.getString("acc")?:"null",
            ppg = data.getString("ppg")?:"null",
            hr = data.getString("hr")?:"null",
            skinTemp = data.getString("skin")?:"null",
        )
        val recentEntities = mutableListOf<RecentEntity>(entity)

        CoroutineScope(Dispatchers.IO).launch {
            recentDao.insertEvents(recentEntities)
        }

        // Send data immediately to the server
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val client = OkHttpClient()
        val jsonBody = JSONObject().apply {
            put("timestamp", data.getLong("timestamp"))
            put("acc", data.getString("acc") ?: "null")
            put("ppg", data.getString("ppg") ?: "null")
            put("hr", data.getString("hr") ?: "null")
            put("skinTemp", data.getString("skin") ?: "null")
            put("device_id", deviceId)
        }.toString()

        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("http://logging.iclab.dev/data")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Error uploading recentEntity to server: ${response.message}")
                    }
                }
            }
        })
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