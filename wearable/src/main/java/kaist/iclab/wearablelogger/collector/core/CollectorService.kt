package kaist.iclab.wearablelogger.collector.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.config.BatteryStateReceiver
import kaist.iclab.wearablelogger.uploader.RecentAlarmReceiver
import kaist.iclab.wearablelogger.uploader.UploadAlarmReceiver
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

private const val TAG = "CollectorService"

class CollectorService : Service() {
    private val collectorRepository by inject<CollectorRepository>()
    private lateinit var batteryReceiver: BatteryStateReceiver
    private val channelId = TAG
    private val channelName = "ABCLogger"
    private val channelText = "ABCLogger is collecting your data"

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        collectorRepository.collectors.forEach {
            runBlocking {
                if (it.getStatus()) {
                    it.startLogging()
                }
            }
        }

        // Setup battery receiver
        batteryReceiver = BatteryStateReceiver()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Setup periodic upload worker
        AlarmScheduler.scheduleExactAlarm(this, UploadAlarmReceiver::class.java, TimeUnit.MINUTES.toMillis(10))
        AlarmScheduler.scheduleExactAlarm(this, RecentAlarmReceiver::class.java, TimeUnit.SECONDS.toMillis(10))

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle(channelName)
                .setContentText(channelText)
                .build()
        Log.d(TAG, "onStartCommand")
        startForeground(1, notification)
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)

        AlarmScheduler.cancelAlarm(this, UploadAlarmReceiver::class.java)
        AlarmScheduler.cancelAlarm(this, RecentAlarmReceiver::class.java)
    }
}