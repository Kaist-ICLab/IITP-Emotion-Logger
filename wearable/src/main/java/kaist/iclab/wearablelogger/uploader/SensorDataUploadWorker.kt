package kaist.iclab.wearablelogger.uploader

import android.content.Context
import android.widget.Toast
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kaist.iclab.wearablelogger.collector.core.CollectorRepository
import org.koin.java.KoinJavaComponent.inject

/**
 * Periodically uploads sensor data
 */
class SensorDataUploadWorker(context: Context,
    workerParams: WorkerParameters,
): CoroutineWorker(context, workerParams) {
    private val collectorRepository by inject<CollectorRepository>()
    override suspend fun doWork(): Result {
        val toast = Toast.makeText(applicationContext, "Uploading data...", Toast.LENGTH_SHORT)
        toast.show()

        collectorRepository.upload()
        return Result.success()
    }
}