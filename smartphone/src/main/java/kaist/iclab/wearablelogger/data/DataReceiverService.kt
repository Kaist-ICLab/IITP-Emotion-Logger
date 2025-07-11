package kaist.iclab.wearablelogger.data

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.Strictness
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.util.DataClientPath
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class DataReceiverService: WearableListenerService() {
    companion object {
        private val TAG = DataReceiverService::class.simpleName

        val recentTimestamp = MutableSharedFlow<Long>()
        val recentHREntity = MutableSharedFlow<HREntity?>()
        val recentAccEntity = MutableSharedFlow<AccEntity?>()
        val recentPpgEntity = MutableSharedFlow<PpgEntity?>()
        val recentSkinTempEntity = MutableSharedFlow<SkinTempEntity?>()

        val watchUploadSchedule = MutableSharedFlow<Long>()
        val phoneUploadSchedule = MutableSharedFlow<Long>()
    }

    private val collectorDao by inject<Map<String, DaoWrapper<EntityBase>>>(qualifier = named("collectorDao"))
    private val stateRepository: StateRepository by inject()
    private val dataUploaderRepository: DataUploaderRepository by inject()
    private val ackRepository: AckRepository by inject()

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        dataEventBuffer.forEach { dataEvent ->
            val dataType = dataEvent.dataItem.uri.path?.split("/")
            if(dataType == null)
                return

            val data = DataMapItem.fromDataItem(dataEvent.dataItem).dataMap
            val basePath = "/${dataType[1]}"
            if(basePath == DataClientPath.RECENT_DATA){
                unpackRecentData(data)
            } else if(basePath == DataClientPath.UPLOAD_DATA) {
                CoroutineScope(Dispatchers.IO).launch {
                    unpackDataAsset(data, dataType[2])
                }
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun unpackRecentData(data: DataMap) {
        Log.d(TAG, "received RecentEntity data")
        val entity = JsonObject()

        entity.addProperty("timestamp", data.getLong("timestamp"))
        entity.addProperty("acc", data.getString("acc") ?: "null")
        entity.addProperty("ppg", data.getString("ppg") ?: "null")
        entity.addProperty("hr", data.getString("hr") ?: "null")
        entity.addProperty("skin", data.getString("skin") ?: "null")
        entity.addProperty("watch_upload_schedule", data.getLong("watch_upload_schedule"))

        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()

        CoroutineScope(Dispatchers.IO).launch {
            recentTimestamp.emit(data.getLong("timestamp"))
            recentHREntity.emit(data.getString("hr")?.let { gson.fromJson(it, HREntity::class.java) } )
            recentAccEntity.emit(data.getString("acc")?.let { gson.fromJson(it, AccEntity::class.java) })
            recentPpgEntity.emit(data.getString("ppg")?.let { gson.fromJson(it, PpgEntity::class.java) })
            recentSkinTempEntity.emit(data.getString("skin")?.let { gson.fromJson(it, SkinTempEntity::class.java) })

            watchUploadSchedule.emit(data.getLong("watch_upload_schedule"))
            phoneUploadSchedule.emit(AlarmScheduler.nextUploadSchedule)

            dataUploaderRepository.uploadRecentData(entity)
        }
    }

    private fun unpackDataAsset(data: DataMap, key: String) {
        Log.d(TAG, "unpack($key): $data")
        val asset = data.getAsset("data")!!

        Wearable.getDataClient(this).getFdForAsset(asset)
            .addOnSuccessListener { assetFd ->
                assetFd.inputStream.use { inputStream ->
                    val json = String(inputStream.readBytes())
                    val daoWrapper = collectorDao[key]!!

                    CoroutineScope(Dispatchers.IO).launch {
                        val idRange = daoWrapper.insertEventsFromJson(json)
                        stateRepository.updateSyncTime(key, System.currentTimeMillis())
                        ackRepository.returnAck(key, idRange)
                    }
                }
            }
    }
}