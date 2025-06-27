package kaist.iclab.wearablelogger.uploader

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kaist.iclab.wearablelogger.collector.core.CollectorRepository
import org.koin.java.KoinJavaComponent.inject

private const val TAG = "SensorDataUploadWorker"

/**
 * Periodically uploads sensor data
 */
class SensorDataUploadWorker(context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {
    private val collectorRepository by inject<CollectorRepository>(CollectorRepository::class.java)
    override suspend fun doWork(): Result {
        Log.v(TAG, "doWork()")
        collectorRepository.upload()
        return Result.success()
    }
}