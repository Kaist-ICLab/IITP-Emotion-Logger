package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.daowrapper.HRDaoWrapper
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.BatteryStateRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HRCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val batteryStateRepository: BatteryStateRepository,
    private val configRepository: ConfigRepository,
    private val hrDaoWrapper: HRDaoWrapper
    ,
) : HealthTrackerCollector(context) {
    companion object {
        private val TAG = HRCollector::class.simpleName
    }
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
            }.filter {
                (!batteryStateRepository.isCharging.value || it.timestamp <= batteryStateRepository.chargeStartTimestamp.value)
            }

            Log.d(TAG, "insert ${hrEntities.size} entities")

            CoroutineScope(Dispatchers.IO).launch {
                hrDaoWrapper.insertEvents(hrEntities)
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

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<String> {
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return hrDaoWrapper.getBeforeLast(startId, limit).map { it ->
            gson.toJson(it.second)
        }
    }

    override fun deleteBetween(startId: Long, endId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            hrDaoWrapper.deleteBetween(startId, endId)
            Log.d(TAG, "Flush $key Data between $startId and $endId")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            hrDaoWrapper.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}