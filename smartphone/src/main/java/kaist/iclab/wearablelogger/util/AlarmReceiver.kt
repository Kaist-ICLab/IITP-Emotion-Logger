package kaist.iclab.wearablelogger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kaist.iclab.loggerstructure.core.AlarmScheduler
import kaist.iclab.wearablelogger.data.DataUploaderRepository
import org.koin.java.KoinJavaComponent

class AlarmReceiver: BroadcastReceiver() {
    private val dataUploaderRepository: DataUploaderRepository by KoinJavaComponent.inject(
        DataUploaderRepository::class.java
    )

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm Starting")
        AlarmScheduler.scheduleExactAlarm(context, this::class.java)
        dataUploaderRepository.uploadFullData()
        // 다음 알람 예약 (15분 후)
    }
}