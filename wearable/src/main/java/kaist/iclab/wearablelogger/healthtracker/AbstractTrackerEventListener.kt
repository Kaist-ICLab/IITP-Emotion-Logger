package kaist.iclab.wearablelogger.healthtracker

import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener

private const val TAG = "AbstractTrackerEventListener"

abstract class AbstractTrackerEventListener: TrackerEventListener {

    override fun onError(trackerError: HealthTracker.TrackerError) {
        Log.d(TAG, "onError")
        when (trackerError) {
            HealthTracker.TrackerError.PERMISSION_ERROR -> Log.e(
                TAG,
                "ERROR: Permission Failed"
            )

            HealthTracker.TrackerError.SDK_POLICY_ERROR -> Log.e(
                TAG,
                "ERROR: SDK Policy Error"
            )

//            else -> Log.e(TAG, "ERROR: Unknown ${trackerError.name}")
        }
    }

    override fun onFlushCompleted() {
        Log.d(TAG, "onFlushCompleted")
    }
}