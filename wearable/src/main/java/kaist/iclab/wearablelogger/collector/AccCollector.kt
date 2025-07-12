package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.daowrapper.AccDaoWrapper
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.BatteryStateRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val batteryStateRepository: BatteryStateRepository,
    private val configRepository: ConfigRepository,
    private val accDaoWrapper: AccDaoWrapper,
) : HealthTrackerCollector(context){
    companion object {
        private val TAG = AccCollector::class.simpleName
    }

    override val key = CollectorType.ACC.name

    override val trackerEventListener: HealthTracker.TrackerEventListener = object :
        AbstractTrackerEventListener() {
        override fun onDataReceived(data: List<DataPoint>) {
            val dataReceived = System.currentTimeMillis()
            Log.d(TAG, "$dataReceived, ${data.size}")
            val accEntities = data.map {
                AccEntity(
                    dataReceived = dataReceived,
                    timestamp = it.timestamp,
                    x = convert2SIUnit(it.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X)),
                    y = convert2SIUnit(it.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y)),
                    z = convert2SIUnit(it.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z))
                )
            }.filter {
                (!batteryStateRepository.isCharging.value || it.timestamp <= batteryStateRepository.chargeStartTimestamp.value)
            }

            Log.d(TAG, "insert ${accEntities.size} entities")

            CoroutineScope(Dispatchers.IO).launch {
                accDaoWrapper.insertEvents(accEntities)
            }
        }

        private fun convert2SIUnit(value: Int): Float {
            return (9.81f / (16383.75f / 4.0f)) * value.toFloat()
        }
    }

    override fun initHealthTracker() {
        tracker = healthTrackerRepository.healthTrackingService
            .getHealthTracker(HealthTrackerType.ACCELEROMETER_CONTINUOUS)
    }

    override suspend fun getStatus(): Boolean {
        return configRepository.getSensorStatus("Accelerometer")
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<String> {
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return accDaoWrapper.getBeforeLast(startId, limit).map { it ->
            gson.toJson(it.second)
        }
    }

    override fun deleteBetween(startId: Long, endId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            accDaoWrapper.deleteBetween(startId, endId)
            Log.d(TAG, "Flush $key Data between $startId and $endId")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            accDaoWrapper.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}