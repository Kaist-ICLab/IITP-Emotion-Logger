package kaist.iclab.wearablelogger.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.Strictness
import kaist.iclab.wearablelogger.dao.EnvDao
import kaist.iclab.wearablelogger.dao.RecentDao
import kaist.iclab.wearablelogger.dao.StepDao
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
) {
    companion object {
        private val TAG = this::class.simpleName
    }

    private enum class LogType(val url: String) {
        DATA("data"),
        RECENT("recent"),
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

        runBlocking {
            val stepEntity = stepDao.getLast()
            val envEntity = envDao.getLast()

            jsonObject.addProperty("step", gson.toJson(stepEntity))
            jsonObject.addProperty("env", gson.toJson(envEntity))
        }

        jsonObject.addProperty("device_id", deviceId)
        uploadJSON(jsonObject.toString(), LogType.RECENT)
    }

    @SuppressLint("HardwareIds")
    private fun uploadJSON(jsonString: String, logType: LogType) {
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
                        Log.e(TAG, "Error uploading recentEntity to server: ${response.message}")
                    }
                }
            }
        })
    }
}