package kaist.iclab.wearablelogger.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.Strictness
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.util.CollectorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.java.KoinJavaComponent.inject
import java.io.IOException
import java.lang.Thread.sleep
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class DataUploaderRepository(
    private val stepDao: StepDao,
    private val envDao: EnvDao,
    private val dataDao: Map<String, DaoWrapper<EntityBase>>,
    private val stateRepository: StateRepository,
) {
    private enum class LogType(val url: String) {
        ERROR(""),
        RECENT("recent"),
        ACC("acc"),
        ENV("env"),
        HR("hr"),
        PPG("ppg"),
        SKINTEMP("skintemp"),
        STEP("step"),
        WATCH_CONNECTION("watch_connection"),
        EXCEPTION("exception"),
    }

    companion object {
        private val TAG = DataUploaderRepository::class.simpleName
        private val TIME_PROPERTY = listOf("timestamp", "dataReceived", "startTime", "endTime")
        private const val CHUNK_SIZE = 2000
    }

    private val deviceInfoRepository: DeviceInfoRepository by inject(DeviceInfoRepository::class.java)
    private val deviceId = deviceInfoRepository.deviceId

    init {
        CoroutineScope(Dispatchers.IO).launch {
            deviceInfoRepository.getWearablesFlow().collect { value ->
                uploadWatchConnectionStatus(value !== null)
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

    fun uploadRecentData(recentEntity: JsonObject) {
        Log.d(TAG, "Upload recent Data")
        val gson = getGson()

        // Convert strings to JSON
        val properties = listOf("acc", "ppg", "hr", "skin")
        for(key in properties) {
            val rawInnerJSON = recentEntity.get(key).toString()
            val innerJSON = rawInnerJSON.replace("\\", "").trim('"')

            if(innerJSON == "null") recentEntity.add(key, null)
            else recentEntity.add(key, JsonParser.parseString(innerJSON).asJsonObject)
        }

        runBlocking {
            val stepEntity = stepDao.getLast()
            val envEntity = envDao.getLast()

            recentEntity.add("step", gson.toJsonTree(stepEntity))
            recentEntity.add("env", gson.toJsonTree(envEntity))
        }

        recentEntity.addProperty("device_id", deviceId)
        recentEntity.addProperty("timestamp", toZonedTimestamp(recentEntity["timestamp"].asLong))
        recentEntity.remove("id")
        uploadJSON(recentEntity.toString(), LogType.RECENT)
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
                    for(daoEntry in dao.getBeforeLast(CHUNK_SIZE)) {
                        val threshold = daoEntry.first
                        val entries = daoEntry.second

                        val data = gson.toJsonTree(entries).asJsonArray
                        data.map { it.asJsonObject.formatForUpload() }

                        Log.d(TAG, "Upload full Data ($chunkIndex): $name")
                        uploadJSON(data.toString(), logType, retryAtTimeout = true)
                        sleep(7000)

                        stateRepository.updateUploadTime(name, System.currentTimeMillis())
                        dao.deleteBefore(threshold)

                        Log.d(TAG, "Delete $logType Data before ${TimeUtil.timestampToString(threshold)}")
                        chunkIndex++
                    }

                } catch(e: Exception) {
                    e.printStackTrace()
                    uploadException(e.stackTraceToString())
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

    fun uploadWatchConnectionStatus(isConnected: Boolean) {
        val data = JsonObject()
        data.addProperty("is_connected", isConnected)
        data.addProperty("device_id", deviceId)
        uploadJSON(getGson().toJson(data), LogType.WATCH_CONNECTION)

    }

    fun uploadException(exception: String) {
        val data = JsonObject()
        data.addProperty("exception", exception)
        data.addProperty("device_id", deviceId)
        uploadJSON(getGson().toJson(data), LogType.EXCEPTION)
    }

    private fun getGson(): Gson {
        return GsonBuilder().setStrictness(Strictness.LENIENT).create()
    }

    private fun uploadJSON(jsonString: String, logType: LogType, retryAtTimeout: Boolean = false) {
        if(logType == LogType.ERROR) {
            Log.e(TAG, "Invalid LogType")
            return
        }

        val client = OkHttpClient()
        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonString.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("http://logging.iclab.dev/${logType.url}")
            .post(requestBody)
            .build()

        var shouldTryUpload = true
        var timeoutMillis = 5000L
        val maxTimeoutMillis = 20000L

        while(shouldTryUpload) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "${logType.url}: Error uploading to server: ${response.message}")
                    } else {
                        Log.d(TAG, "${logType.url}: Upload successful")
                    }

                    shouldTryUpload = retryAtTimeout && !response.isSuccessful
                }

                if(!shouldTryUpload) break

            } catch (e: IOException) {
                Log.e(TAG, "${logType.url}: Upload failed - ${e.message}")
                e.printStackTrace()

                if(logType !== LogType.EXCEPTION) {
                    uploadException(e.toString())
                }
            }

            sleep(timeoutMillis)
            timeoutMillis = (timeoutMillis * 2).coerceAtMost(maxTimeoutMillis)
        }
    }

    private fun toZonedTimestamp(timeMillis: Long): String {
        val kstTime = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timeMillis),
            ZoneId.of("Asia/Seoul")
        )

        val string = kstTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return string
    }
}