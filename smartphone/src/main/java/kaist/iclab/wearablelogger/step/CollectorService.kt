package kaist.iclab.wearablelogger.step

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

private const val TAG = "CollectorService"

class CollectorService : Service() {
//    private val collectorRepository by inject<CollectorRepository>()
    private val stepCollector by inject<StepCollector>()
    private val channelId = TAG
    private val channelName = "ABCLogger_mobile"
    private val channelText = "ABCLogger is collecting your data"

    init {
        stepCollector.setup()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        Log.v(TAG, "Service Started")

        runBlocking {
            if(stepCollector.getStatus())
                stepCollector.startLogging()
        }

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle(channelName)
                .setContentText(channelText)
                .build()
        Log.d(TAG, "onStartCommand")
        startForeground(2, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(
            NotificationManager::class.java
        )
        manager.createNotificationChannel(channel)
    }
}