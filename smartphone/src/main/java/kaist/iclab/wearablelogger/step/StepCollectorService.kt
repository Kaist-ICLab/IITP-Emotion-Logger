package kaist.iclab.wearablelogger.step

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kaist.iclab.wearablelogger.util.ForegroundNotification
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

private const val TAG = "StepCollectorService"

class StepCollectorService : Service() {
    private val stepCollector: StepCollector by inject()
    private val stateRepository: StateRepository by inject()

    init {
        stepCollector.setup()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(TAG, "Service Started")

        CoroutineScope(Dispatchers.IO).launch {
            if (stepCollector.getStatus()) {
                stepCollector.startLogging()
                stateRepository.updateIsStepCollected(true)
            }
        }

        val notification = ForegroundNotification.getNotification(this)
        Log.d(TAG, "onStartCommand")
        startForeground(2, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            stateRepository.updateIsStepCollected(false)
        }
        stepCollector.stopLogging()
    }
}