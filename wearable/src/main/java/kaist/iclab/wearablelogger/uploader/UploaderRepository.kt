package kaist.iclab.wearablelogger.uploader

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kaist.iclab.loggerstructure.core.CollectorInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.javaClass

private const val TAG = "UploaderRepository"

class UploaderRepository(
    private val androidContext: Context,
) {
    private val dataPath = "/WEARABLE_DATA"
    fun upload2Phone(collector: CollectorInterface) {
        Log.d(TAG, "sendData2Phone")
        val dataClient = Wearable.getDataClient(androidContext)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1.5h worth of SQL is very likely to be >100KB
                // So convert to Asset
                Log.d(TAG, "${collector.key} data: ${collector.stringifyData()}")
                val asset = Asset.createFromBytes(collector.stringifyData().toByteArray())
                val request = PutDataMapRequest.create(dataPath).run {
                    dataMap.putString("key", collector.key)
                    dataMap.putAsset(collector.key, asset)
                    asPutDataRequest()
                }
                dataClient.putDataItem(request).await()
                Log.d(TAG, "${collector.key} Data has been uploaded")
                collector.flush()
            } catch (exception: Exception) {
                Log.e(TAG, "Saving DataItem failed: $exception")
            }
        }
    }

    suspend fun sync2Server(data: String) {
        Log.d(TAG, "sync2Server")

        val api = RetrofitClient.getRetrofit().create(ServerAPIInterface::class.java)
        try{
            api.postData(data).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d(TAG, response.message())
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            })
        } catch (e: Exception){
            Log.e(TAG, "$e")
        }

    }
}