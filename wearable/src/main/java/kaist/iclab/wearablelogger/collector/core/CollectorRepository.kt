package kaist.iclab.wearablelogger.collector.core

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import kaist.iclab.loggerstructure.core.CollectorInterface
import kaist.iclab.wearablelogger.config.BatteryStateRepository
import kaist.iclab.wearablelogger.uploader.UploaderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val TAG = "CollectorRepository"

class CollectorRepository(
    val collectors: List<CollectorInterface>,
    val uploaderRepository: UploaderRepository,
    val batteryStateRepository: BatteryStateRepository,
    val androidContext: Context
) {
    init {
        collectors.forEach {
            it.setup()
        }

        CoroutineScope(Dispatchers.IO).launch {
            combine(
                batteryStateRepository.isCharging,
                batteryStateRepository.batteryLevel,
            ) { isCharging, batteryLevel -> Pair(isCharging, batteryLevel) }
                .collect {
                    uploaderRepository.uploadBatteryData(it.first, it.second)
                }
        }
    }

    fun start() {
        val intent = Intent(androidContext, CollectorService::class.java)
        ContextCompat.startForegroundService(androidContext, intent)
        Log.d(TAG, "start")
    }

    fun stop() {
        val intent = Intent(androidContext, CollectorService::class.java)

        androidContext.stopService(intent)
        collectors.onEach {
            it.stopLogging()
        }
        Log.d(TAG, "stop")
    }

    fun flush(){
        collectors.forEach {
            it.flush()
        }
    }

    fun upload(){
        CoroutineScope(Dispatchers.IO).launch {
            collectors.forEach {collector ->
                runBlocking {
                    uploaderRepository.uploadFullData(collector)
                }
            }
        }
    }
    
    fun uploadRecent() {
        CoroutineScope(Dispatchers.IO).launch {
            uploaderRepository.uploadRecentData()
        }
    }
}