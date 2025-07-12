package kaist.iclab.wearablelogger.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.data.DataUploaderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugViewModel(
    private val uploaderRepository: DataUploaderRepository,
): ViewModel() {
    companion object {
        private val TAG = DebugViewModel::class.simpleName
    }

    fun uploadSingleStepEntity() {
        val stepEntity = StepEntity(
            id = 0,
            dataReceived = 0,
            startTime = 0,
            endTime = 1000000,
            step = 42,
        )

        CoroutineScope(Dispatchers.IO).launch {
            uploaderRepository.uploadSingleEntity(stepEntity, CollectorType.STEP)
        }
    }

    fun flush() {
        Log.d(TAG, "flush()")
        CoroutineScope(Dispatchers.IO).launch {
            uploaderRepository.uploadFullData()
        }
    }
}