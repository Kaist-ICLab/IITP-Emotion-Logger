package kaist.iclab.wearablelogger.util

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.java.KoinJavaComponent.inject
import kotlin.getValue

class SensorDataUploadWorker(
    context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {
    companion object {
        private val TAG = this::class.simpleName
    }

    private val collectorRepository by inject<DataUploaderRepository>(DataUploaderRepository::class.java)
    override suspend fun doWork(): Result {
        Log.v(TAG, "doWork()")
        collectorRepository.uploadFullData()
        return Result.success()
    }
}