package kaist.iclab.wearablelogger.collector.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.koin.java.KoinJavaComponent.inject

class AlarmReceiver: BroadcastReceiver() {
    private val collectorRepository: CollectorRepository by inject(CollectorRepository::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm Starting")
        collectorRepository.upload()

        // 다음 알람 예약 (15분 후)
        AlarmScheduler.scheduleExactAlarm(context)
    }
}