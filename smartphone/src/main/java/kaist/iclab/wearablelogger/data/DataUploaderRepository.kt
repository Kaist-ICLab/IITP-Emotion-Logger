package kaist.iclab.wearablelogger.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.Strictness
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.util.DeviceInfoRepository
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DataUploaderRepository(
    private val dataDao: Map<String, DaoWrapper<EntityBase>>,
    private val stateRepository: StateRepository,
    deviceInfoRepository: DeviceInfoRepository
) {
    private enum class LogType(val url: String) {
        ERROR(""),
//        RECENT("recent"),
        ACC("acc"),
        ENV("env"),
        HR("hr"),
        PPG("ppg"),
        SKINTEMP("skintemp"),
        STEP("step"),
        DEVICE_STATUS("device_status"),
        EXCEPTION("exception"),
    }

    companion object {
        private val TAG = DataUploaderRepository::class.simpleName
        private val TIME_PROPERTY = listOf("timestamp", "dataReceived", "startTime", "endTime", "watch_upload_schedule", "phone_upload_schedule", "bucket_start")
        private const val CHUNK_SIZE = 2000L
    }

    private val deviceId = deviceInfoRepository.deviceId
    private val wearables = deviceInfoRepository.getWearablesFlow()
    private val phoneUploadSchedule = deviceInfoRepository.phoneUploadSchedule
    private val watchUploadSchedule = deviceInfoRepository.watchUploadSchedule
    private val isWearableCharging = deviceInfoRepository.isWearableCharging
    private val wearableBatteryLevel = deviceInfoRepository.wearableBatteryLevel

    init {
        CoroutineScope(Dispatchers.IO).launch {
            combine(
                wearables,
                phoneUploadSchedule,
                watchUploadSchedule,
                isWearableCharging,
                wearableBatteryLevel
            ) { _, _, _ , _, _-> }
                .collect {
                    uploadDeviceStatus()
                }
        }
    }

    private fun JsonObject.formatForUpload() {
        this.addProperty("device_id", deviceId)
        for(prop in TIME_PROPERTY) {
            if(this.has(prop))
                this.addProperty(prop, toZonedTimestamp(this[prop].asLong))
        }

        this.remove("id")
    }

    private fun nameToLogType(name: String): LogType {
        val logType = when(name) {
            CollectorType.SKINTEMP.name -> LogType.SKINTEMP
            CollectorType.ACC.name -> LogType.ACC
            CollectorType.ENV.name -> LogType.ENV
            CollectorType.PPG.name -> LogType.PPG
            CollectorType.STEP.name -> LogType.STEP
            CollectorType.HR.name -> LogType.HR
            else -> LogType.ERROR
        }

        return logType
    }

//    suspend fun uploadRecentData(recentEntity: JsonObject) {
//        Log.d(TAG, "Upload recent Data")
//        val gson = getGson()
//
//        // Convert strings to JSON
//        val properties = listOf("acc", "ppg", "hr", "skin")
//        for(key in properties) {
//            val rawInnerJSON = recentEntity.get(key).toString()
//            val innerJSON = rawInnerJSON.replace("\\", "").trim('"')
//
//            if(innerJSON == "null") recentEntity.add(key, null)
//            else recentEntity.add(key, JsonParser.parseString(innerJSON).asJsonObject)
//        }
//
//        val stepEntity = stepDao.getLast()
//        val envEntity = envDao.getLast()
//        recentEntity.add("step", gson.toJsonTree(stepEntity))
//        recentEntity.add("env", gson.toJsonTree(envEntity))
//        recentEntity.formatForUpload()
//
//        uploadJSON(recentEntity.toString(), LogType.RECENT)
//    }

    fun uploadSummaryData() {
        val gson = getGson()
        for(entry in dataDao){
            val name = entry.key
            val dao = entry.value
            val logType = nameToLogType(name)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data = gson.toJsonTree(dao.getSummary()).asJsonArray
                    data.map { it.asJsonObject.formatForUpload() }
                    uploadJSON(data.toString(), logType, isSummary = true)

                    Log.d(TAG, "Upload summary Data: $name")
                } catch(e: Exception) {
                    e.printStackTrace()
                    uploadException(e)
                }
            }
            Thread.sleep(7000)
        }
    }

    fun uploadFullData() {
        val gson = getGson()

        CoroutineScope(Dispatchers.IO).launch {
            for(entry in dataDao){
                val name = entry.key
                val dao = entry.value
                val logType = nameToLogType(name)

                var chunkIndex = 0
                try {
                    for(daoEntry in dao.getBeforeLast(0, CHUNK_SIZE)) {
                        val idRange = daoEntry.first
                        val entries = daoEntry.second

                        val data = gson.toJsonTree(entries).asJsonArray
                        data.map { it.asJsonObject.formatForUpload() }

                        Log.d(TAG, "Upload full Data ($chunkIndex): $name")
                        val success = uploadJSON(data.toString(), logType, retryAtTimeout = true)
                        if(success)
                            CoroutineScope(Dispatchers.IO).launch {
                                Log.d(TAG, "Delete $logType Data: $idRange")
                                dao.deleteBetween(idRange.startId, idRange.endId)
                            }

                        stateRepository.updateUploadTime(name, System.currentTimeMillis())
                        chunkIndex++
                        Thread.sleep(7000)
                    }

                } catch(e: Exception) {
                    e.printStackTrace()
                    uploadException(e)
                }
            }
        }
    }

    fun uploadSingleEntity(entity: EntityBase, collectorType: CollectorType) {
        val gson = getGson()
        val data = gson.toJsonTree(entity)
        data.asJsonObject.formatForUpload()

        uploadJSON(data.toString(), nameToLogType(collectorType.name))
    }

    fun uploadDeviceStatus() {
        val data = JsonObject()
        data.addProperty("device_id", deviceId)
        data.addProperty("is_connected", wearables.value !== null)
        data.addProperty("watch_upload_schedule", watchUploadSchedule.value)
        data.addProperty("phone_upload_schedule", phoneUploadSchedule.value)
        data.addProperty("wearable_is_charging", isWearableCharging.value)
        data.addProperty("wearable_battery_level", wearableBatteryLevel.value)
        data.formatForUpload()
        uploadJSON(getGson().toJson(data), LogType.DEVICE_STATUS)
    }

    fun uploadException(exception: Exception) {
        val data = JsonObject()
        data.addProperty("exception", exception.stackTraceToString())
        data.addProperty("timestamp", System.currentTimeMillis())
        data.formatForUpload()
        uploadJSON(getGson().toJson(data), LogType.EXCEPTION)
    }

    private fun getGson(): Gson {
        return GsonBuilder().setStrictness(Strictness.LENIENT).create()
    }

    private fun uploadJSON(jsonString: String, logType: LogType, retryAtTimeout: Boolean = false, isSummary: Boolean = false): Boolean {
        if(logType == LogType.ERROR) {
            Log.e(TAG, "Invalid LogType")
            return false
        }

        val client = OkHttpClient()
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(jsonMediaType)

        val url = logType.url + (if(isSummary) "_summary" else "")
        val request = Request.Builder()
            .url("http://logging.iclab.dev/${url}")
            .post(requestBody)
            .build()

        var shouldTryUpload = true
        var retryCount = 0
        var timeoutMillis = 4000L
        val maxRetryCount = 3
        val maxTimeoutMillis = 32000L

        while(true) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "${logType.url}: Error uploading to server: ${response.message}")
                    } else {
                        Log.d(TAG, "${logType.url}: Upload successful")
                    }
                }
                
                if(response.isSuccessful) return true

            } catch(e: Exception) {
                e.printStackTrace()

                when(e) {
                    // Internet connection not enabled
                    is UnknownHostException -> {
                        Log.e(TAG, "${logType.url}: Unknown Host - ${e.message}")
                        timeoutMillis = 4000L
                    }
                    // ???
                    is ConnectException -> {
                        Log.e(TAG, "${logType.url}: Failed to connect - ${e.message}")
                        timeoutMillis = 4000L
                    }
                    // The server might be handling too much data!
                    is SocketTimeoutException -> {
                        timeoutMillis = (timeoutMillis * 2).coerceAtMost(maxTimeoutMillis)
                        Log.e(TAG, "${logType.url}: Timeout - ${e.message}")
                    }
                }
            }

            retryCount++
            if(retryAtTimeout) Log.d(TAG, "${logType.url}, retryAtTimeout: $retryAtTimeout, retryCount: $retryCount")
            shouldTryUpload = retryAtTimeout && retryCount <= maxRetryCount
            if(!shouldTryUpload) break

            Thread.sleep(timeoutMillis)
        }

        return false
    }

    private fun toZonedTimestamp(timeMillis: Long): String {
        val kstTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(timeMillis),
            ZoneId.of("Asia/Seoul")
        )

        val string = kstTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return string
    }
}