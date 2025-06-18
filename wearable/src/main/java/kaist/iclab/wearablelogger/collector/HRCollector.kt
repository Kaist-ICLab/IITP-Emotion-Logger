package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "HRCollector"

class HRCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val configRepository: ConfigRepository,
    private val hrDao: HRDao,
) : HealthTrackerCollector(context) {
    override val key = CollectorType.HR.name

    override val trackerEventListener = object :
        AbstractTrackerEventListener() {
        override fun onDataReceived(data: List<DataPoint>) {
            val dataReceived = System.currentTimeMillis()
            Log.d(TAG, "$dataReceived, ${data.size}")
            val hrEntities = data.map {
                HREntity(
                    dataReceived = dataReceived,
                    timestamp = it.timestamp,
                    hr = it.getValue(ValueKey.HeartRateSet.HEART_RATE),
                    hrStatus = it.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS),
                    ibi = it.getValue(ValueKey.HeartRateSet.IBI_LIST),
                    ibiStatus = it.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST),
                )
            }
            CoroutineScope(Dispatchers.IO).launch {
                hrDao.insertEvents(hrEntities)
            }
        }
    }

    override fun initHealthTracker() {
        tracker = healthTrackerRepository.healthTrackingService
            .getHealthTracker(HealthTrackerType.HEART_RATE_CONTINUOUS)
    }

    override suspend fun getStatus(): Boolean {
        return configRepository.getSensorStatus("Heart Rate")
    }

    override suspend fun stringifyData():String{
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return gson.toJson(hrDao.getAll())
    }
    override fun flush() {
        Log.d(TAG, "Flush HR Data")
        CoroutineScope(Dispatchers.IO).launch {
            hrDao.deleteAll()
            Log.d(TAG, "deleteAll() for HR Data")
        }
    }
}