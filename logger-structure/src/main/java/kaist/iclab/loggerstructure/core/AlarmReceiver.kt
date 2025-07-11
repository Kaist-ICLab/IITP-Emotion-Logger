package kaist.iclab.loggerstructure.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

abstract class AlarmReceiver: BroadcastReceiver() {
    private val tag = this::class.simpleName

    abstract fun executeOnAlarm()

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Alarm Starting")
        val period = intent.extras!!.getLong(AlarmScheduler.TIME_PERIOD_KEY)

        AlarmScheduler.scheduleExactAlarm(context, this::class.java, period)
        executeOnAlarm()
    }
}