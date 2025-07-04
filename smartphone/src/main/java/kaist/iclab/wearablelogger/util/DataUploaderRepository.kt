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
import kaist.iclab.loggerstructure.dao.RecentDao
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
    private val recentDao: RecentDao,
    private val stepDao: StepDao,
    private val envDao: EnvDao,
    private val dataDao: Map<String, DaoWrapper<EntityBase>>,
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

    fun uploadRecentData() {
        Log.d(TAG, "Upload recent Data")
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        val recentEntity = recentDao.getLast()
        val jsonObject: JsonObject = gson.toJsonTree(recentEntity).asJsonObject



        // Convert strings to JSON
        val properties = listOf("acc", "ppg", "hr", "skinTemp")
        for(key in properties) {
            val rawInnerJSON = jsonObject.get(key).toString()
            val innerJSON = rawInnerJSON.replace("\\", "").trim('"')

            if(innerJSON == "null") jsonObject.add(key, null)
            else jsonObject.add(key, JsonParser.parseString(innerJSON).asJsonObject)
        }

        runBlocking {
            val stepEntity = stepDao.getLast()
            val envEntity = envDao.getLast()

            jsonObject.add("step", gson.toJsonTree(stepEntity))
            jsonObject.add("env", gson.toJsonTree(envEntity))
        }

        jsonObject.addProperty("device_id", deviceId)
        jsonObject.addProperty("timestamp", toTimestampz(jsonObject["timestamp"].asLong))
        jsonObject.remove("id")
        uploadJSON(jsonObject.toString(), LogType.RECENT)
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

                val data = gson.toJsonTree(dao.getAll()).asJsonArray
                dao.deleteAll()

                Log.d(TAG, "${logType.url} data length: ${data.size()}")

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
                for(chunk in chunks) {
                    val json = JsonObject()
                    json.add("data", JsonArray().apply { chunk.forEach { add(it) }})
                    json.addProperty("device_id", deviceId)

                    Log.d(TAG, "Upload full Data: $name")
                    uploadJSON(json.toString(), logType)
                    sleep(9000)
                }
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
        while(shouldTryUpload) {
            try {
                val response = client.newCall(request).execute()
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "${logType.url}: Error uploading to server: ${response.message}")
                        sleep(5000)
                    } else {
                        Log.d(TAG, "${logType.url}: Upload successful")
                    }

                    shouldTryUpload = retryAtTimeout && !response.isSuccessful
                }
            } catch (e: IOException) {
                Log.e(TAG, "${logType.url}: Upload failed - ${e.message}")
                e.printStackTrace()
                sleep(5000)
            }
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