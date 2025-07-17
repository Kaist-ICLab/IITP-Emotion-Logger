package kaist.iclab.wearablelogger.data

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.util.ForegroundNotification
import java.util.concurrent.TimeUnit

class DataUploaderService: Service() {
    companion object {
        private val TAG = DataUploaderService::class.simpleName
    }
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Setup periodic upload worker
        AlarmScheduler.scheduleExactAlarm(this, UploadAlarmReceiver::class.java, TimeUnit.MINUTES.toMillis(15))

        val notification = ForegroundNotification.getNotification(this)
        Log.d(TAG, "onStartCommand")
        startForeground(2, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmScheduler.cancelAlarm(this, UploadAlarmReceiver::class.java)
    }
}