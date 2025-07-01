package kaist.iclab.wearablelogger.step

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kaist.iclab.wearablelogger.util.ForegroundNotification
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

private const val TAG = "StepCollectorService"

class StepCollectorService : Service() {
    private val stepCollector by inject<StepCollector>()

    init {
        stepCollector.setup()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(TAG, "Service Started")

        runBlocking {
            if(stepCollector.getStatus())
                stepCollector.startLogging()
        }

        val notification = ForegroundNotification.getNotification(this)
        Log.d(TAG, "onStartCommand")
        startForeground(2, notification)
        return START_STICKY
    }
}