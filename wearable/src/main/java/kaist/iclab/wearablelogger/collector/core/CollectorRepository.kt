package kaist.iclab.wearablelogger.collector.core

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import kaist.iclab.loggerstructure.core.CollectorInterface
import kaist.iclab.wearablelogger.collector.core.CollectorService
import kaist.iclab.wearablelogger.uploader.UploaderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "CollectorRepository"

class CollectorRepository(
    val collectors: List<CollectorInterface>,
    val uploaderRepository: UploaderRepository,
    val androidContext: Context
) {

    init {
        collectors.forEach {
            it.setup()
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
//                uploaderRepository.sync2Server(data)
                uploaderRepository.upload2Phone(collector)
            }
        }
    }
}