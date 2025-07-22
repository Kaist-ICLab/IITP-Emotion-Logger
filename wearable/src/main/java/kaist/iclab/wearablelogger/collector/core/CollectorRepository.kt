package kaist.iclab.wearablelogger.collector.core

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import kaist.iclab.loggerstructure.core.CollectorInterface

private const val TAG = "CollectorRepository"

class CollectorRepository(
    val collectors: List<CollectorInterface>,
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

    fun deleteBetween(startTime: Long, endTime: Long) {
        collectors.forEach {
            it.deleteBetween(startTime, endTime)
        }
    }

    fun flush(){
        collectors.forEach {
            it.flush()
        }
    }
}