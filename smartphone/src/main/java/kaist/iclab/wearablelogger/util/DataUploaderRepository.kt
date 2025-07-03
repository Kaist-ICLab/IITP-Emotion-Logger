package kaist.iclab.wearablelogger.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.gson.GsonBuilder
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
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class DataUploaderRepository(
    val context: Context,
    val recentDao: RecentDao,
    val stepDao: StepDao,
    val envDao: EnvDao,
    val dataDao: Map<String, DaoWrapper<EntityBase>>
) {
    companion object {
        private val TAG = DataUploaderRepository::class.simpleName
    }

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

    @SuppressLint("HardwareIds")
    var deviceId: String = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

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
            Log.d(TAG, innerJSON)

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
        uploadJSON(jsonObject.toString(), LogType.RECENT)
    }

    fun uploadFullData() {
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        for(entry in dataDao){
            val name = entry.key
            val dao = entry.value

            CoroutineScope(Dispatchers.IO).launch {
                val data = gson.toJsonTree(dao.getAll())
                dao.deleteAll()

                val json = JsonObject()
                json.add("data", data)
                json.addProperty("device_id", deviceId)

                val logType = when(name) {
                    CollectorType.SKINTEMP.name -> LogType.SKINTEMP
                    CollectorType.ACC.name -> LogType.ACC
                    CollectorType.ENV.name -> LogType.ENV
                    CollectorType.PPG.name -> LogType.PPG
                    CollectorType.STEP.name -> LogType.STEP
                    CollectorType.HR.name -> LogType.HR
                    else -> LogType.ERROR
                }

                uploadJSON(json.toString(), logType)
            }
        }
    }

    @SuppressLint("HardwareIds")
    private fun uploadJSON(jsonString: String, logType: LogType) {
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "${logType.url}: Error uploading to server: ${response.message}")
                    }
                }
            }
        })
    }
}