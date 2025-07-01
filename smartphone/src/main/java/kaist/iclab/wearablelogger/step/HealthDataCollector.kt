package kaist.iclab.wearablelogger.step

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import kaist.iclab.loggerstructure.core.CollectorInterface
import kaist.iclab.loggerstructure.core.PermissionUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

private const val TAG = "HealthDataCollector"

abstract class HealthDataCollector(
    private val context: Context
): CollectorInterface {
    var store: HealthDataStore? = null
    private var job: Job? = null

    abstract suspend fun CoroutineScope.logData()
    override fun setup() {
        store = HealthDataService.getStore(context)
        Log.v(TAG, "HealthDataService setup")
    }

    override fun isAvailable(): Boolean {
        return PermissionUtil.isPermissionAllowed(
            context, listOfNotNull(
                Manifest.permission.BODY_SENSORS,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.BODY_SENSORS_BACKGROUND else null,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )
    }
    override fun startLogging() {
        Log.v(TAG, "HealthDataCollector startLogging()")
        try{
            job = CoroutineScope(Dispatchers.IO).launch {
                while(isActive) {
                    logData()
                    sleep(5000)
                }
            }
        }catch(e: Exception){
            Log.e(TAG, "HealthDataCollector startLogging: $e")
        }
    }

    override fun stopLogging() {
        Log.d(TAG, "stopLogging")
        job?.cancel()
        job = null
    }
}