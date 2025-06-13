package kaist.iclab.wearablelogger.collector

//import android.Manifest
//import android.content.Context
//import android.os.Build
//import android.util.Log
//import kaist.iclab.wearablelogger.config.Util
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//
//abstract class HealthDataCollector(
//    private val context: Context
//): CollectorInterface {
//    override val TAG = javaClass.simpleName
//    var store: HealthDataStore? = null
//    private var job: Job? = null
//
//    abstract suspend fun CoroutineScope.logData()
//    override fun setup() {
//        store = HealthDataService.getStore(context)
//        Log.e(TAG, "HealthDataService setup")
//    }
//    override fun isAvailable(): Boolean {
//            return Util.isPermissionAllowed(
//                context, listOfNotNull(
//                    Manifest.permission.BODY_SENSORS,
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.BODY_SENSORS_BACKGROUND else null,
//                    Manifest.permission.ACTIVITY_RECOGNITION
//                )
//            )
//    }
//    override fun startLogging() {
//        try{
//            job = CoroutineScope(Dispatchers.IO).launch {
//                while(isActive)
//                logData()
//            }
//        }catch(e: Exception){
//            Log.e(TAG, "SkinTempCollector startLogging: $e")
//        }
//    }
//
//    override fun stopLogging() {
//        Log.d(TAG, "stopLogging")
//        job?.cancel()
//        job = null
//    }
//
//}