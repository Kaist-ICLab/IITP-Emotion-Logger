package kaist.iclab.wearablelogger.util

import android.annotation.SuppressLint
import android.util.Log
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
    companion object {
        private val TAG = DataUploaderRepository::class.simpleName
    }

    private val deviceInfoRepository: DeviceInfoRepository by inject(DeviceInfoRepository::class.java)
    private val deviceId = deviceInfoRepository.deviceId

    private enum class LogType(val url: String) {
        ERROR(""),
        RECENT("recent"),
        ACC("acc"),
        ENV("env"),
        HR("hr"),
        PPG("ppg"),
        SKINTEMP("skintemp"),
        STEP("step")
    }

    fun uploadRecentData(recentEntity: JsonObject) {
        Log.d(TAG, "Upload recent Data")
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()

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
        recentEntity.addProperty("timestamp", toTimestampz(recentEntity["timestamp"].asLong))
        recentEntity.remove("id")
        uploadJSON(recentEntity.toString(), LogType.RECENT)
    }

    fun uploadFullData() {
        val chunkSize = 2000
        val timeProperty = listOf("timestamp", "dataReceived", "startTime", "endTime")
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()

        CoroutineScope(Dispatchers.IO).launch {
            for(entry in dataDao){
                val name = entry.key
                val dao = entry.value

                val logType = when(name) {
                    CollectorType.SKINTEMP.name -> LogType.SKINTEMP
                    CollectorType.ACC.name -> LogType.ACC
                    CollectorType.ENV.name -> LogType.ENV
                    CollectorType.PPG.name -> LogType.PPG
                    CollectorType.STEP.name -> LogType.STEP
                    CollectorType.HR.name -> LogType.HR
                    else -> LogType.ERROR
                }

                val pair = dao.getBeforeLast()
                val threshold = pair.first
                val entries = pair.second

                val data = gson.toJsonTree(entries).asJsonArray

                for(elem in data) {
                    val elemObject = elem.asJsonObject
                    elemObject.addProperty("device_id", deviceId)
                    for(prop in timeProperty) {
                        if(elemObject.has(prop))
                            elemObject.addProperty(prop, toTimestampz(elemObject[prop].asLong))
                    }

                    elemObject.remove("id")
                }

                val chunks = data.chunked(chunkSize)
                var chunkIndex = 1
                for(chunk in chunks) {
                    val json = JsonObject()
                    json.add("data", JsonArray().apply { chunk.forEach { add(it) }})
                    json.addProperty("device_id", deviceId)

                    Log.d(TAG, "Upload full Data ($chunkIndex / ${chunks.size}): $name")
                    uploadJSON(json.toString(), logType)
                    sleep(7000)
                    chunkIndex += 1
                }

                stateRepository.updateUploadTime(name, System.currentTimeMillis())
                dao.deleteBefore(threshold)
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun uploadJSON(jsonString: String, logType: LogType) {
        if(logType == LogType.ERROR) {
            Log.e(TAG, "Invalid LogType")
            return
        }

        val retryAtTimeout = (logType != LogType.RECENT)

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

        while(true) {
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
            }

            sleep(timeoutMillis)
            timeoutMillis = (timeoutMillis * 2).coerceAtMost(maxTimeoutMillis)
        }
    }

    private fun toTimestampz(timeMillis: Long): String {
        val kstTime = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timeMillis),
            ZoneId.of("Asia/Seoul")
        )

        val string = kstTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        return string
    }
}