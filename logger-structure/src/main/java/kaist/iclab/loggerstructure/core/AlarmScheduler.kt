package kaist.iclab.loggerstructure.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import kaist.iclab.loggerstructure.util.TimeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AlarmScheduler {
    private val TAG = this::class.simpleName
    private const val REQUEST_CODE = 12345
    const val TIME_PERIOD_KEY = "period"

    private val _nextAlarmSchedule = MutableStateFlow<Map<String, Long>>(mapOf())
    val nextAlarmSchedule = _nextAlarmSchedule.asStateFlow()

    fun scheduleExactAlarm(context: Context, cls: Class<out AlarmReceiver>, timePeriod: Long) {
        Log.d(TAG, "scheduleExactAlarm()")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, cls).apply {
            putExtra(TIME_PERIOD_KEY, timePeriod)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextAlarm = System.currentTimeMillis() + timePeriod
        _nextAlarmSchedule.value = _nextAlarmSchedule.value.toMutableMap().apply {
            put(cls.simpleName, nextAlarm)
        }

        Log.d(TAG, "next alarm: ${TimeUtil.timestampToString(nextAlarm)}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextAlarm,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, cls: Class<out AlarmReceiver>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, cls)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}