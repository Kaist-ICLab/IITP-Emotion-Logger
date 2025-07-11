package kaist.iclab.loggerstructure.core

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

object AlarmScheduler {
    private val TAG = this::class.simpleName
    private const val REQUEST_CODE = 12345
    private const val ALARM_PERIOD = 15 * 60 * 1000 // 15min

    var nextUploadSchedule = -1L

    fun scheduleExactAlarm(context: Context, cls: Class<out BroadcastReceiver>) {
        Log.d(TAG, "scheduleExactAlarm()")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, cls)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        nextUploadSchedule = System.currentTimeMillis() + ALARM_PERIOD
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextUploadSchedule,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, cls: Class<out BroadcastReceiver>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, cls)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        nextUploadSchedule = -1
    }
}