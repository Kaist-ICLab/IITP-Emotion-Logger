package kaist.iclab.wearablelogger.collector.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.MyDataRoomDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

private const val TAG = "CollectorService"

class CollectorService : Service() {
    private val collectorRepository by inject<CollectorRepository>()
    private val db by inject<MyDataRoomDB>()
    private val channelId = TAG
    private val channelName = "ABCLogger"
    private val channelText = "ABCLogger is collecting your data"
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private var job: Job? = null

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

        // Cancel existing job
        if(job != null) job?.cancel()

        // Periodically send last data collected
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(TimeUnit.SECONDS.toMillis(5))
                val request = PutDataMapRequest.create("/WEARABLE").apply {
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                    dataMap.putString("acc", Gson().toJson(db.accDao().getLast()))
                    dataMap.putString("hr", Gson().toJson(db.hrDao().getLast()))
                    dataMap.putString("ppg", Gson().toJson(db.ppgDao().getLast()))
                    dataMap.putString("skin", Gson().toJson(db.skinTempDao().getLast()))
                    dataMap.putLong("watch_upload_schedule", AlarmScheduler.nextUploadSchedule)
                }.asPutDataRequest().setUrgent()
                val result = dataClient.putDataItem(request).await()
                Log.d(TAG, "COLLECTOR SEND $result")
            }
        }

        // Setup periodic upload worker
        AlarmScheduler.scheduleExactAlarm(this, AlarmReceiver::class.java)

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
        job?.cancel()
        job = null

        AlarmScheduler.cancelAlarm(this, AlarmReceiver::class.java)
    }
}