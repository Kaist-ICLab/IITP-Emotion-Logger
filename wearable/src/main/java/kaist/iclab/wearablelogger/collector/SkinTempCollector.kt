package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.daowrapper.SkinTempDaoWrapper
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.BatteryStateRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SkinTempCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val batteryStateRepository: BatteryStateRepository,
    private val configRepository: ConfigRepository,
    private val skinTempDaoWrapper: SkinTempDaoWrapper
) : HealthTrackerCollector(context) {
    companion object {
        private val TAG = SkinTempCollector::class.simpleName
    }

    override val key = CollectorType.SKINTEMP.name

    override fun initHealthTracker() {
        tracker = healthTrackerRepository.healthTrackingService
            .getHealthTracker(HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS)
    }

    override suspend fun getStatus(): Boolean {
        return configRepository.getSensorStatus("Skin Temperature")
    }

    override val trackerEventListener = object :
        AbstractTrackerEventListener() {
        override fun onDataReceived(data: List<DataPoint>) {
            val dataReceived = System.currentTimeMillis()
            Log.d(TAG, "$dataReceived, ${data.size}")
            val skinTempData = data.map {
                SkinTempEntity(
                    dataReceived = dataReceived,
                    timestamp = it.timestamp,
                    ambientTemp = it.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE),
                    objectTemp = it.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE),
                    status = it.getValue(ValueKey.SkinTemperatureSet.STATUS)
                )
            }.filter {
                (!batteryStateRepository.isCharging.value || it.timestamp <= batteryStateRepository.chargeStartTimestamp.value)
            }

            Log.d(TAG, "insert ${skinTempData.size} entities")

            CoroutineScope(Dispatchers.IO).launch {
                skinTempDaoWrapper.insertEvents(skinTempData)
            }
        }
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<String> {
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return skinTempDaoWrapper.getBeforeLast(startId, limit).map { it ->
            gson.toJson(it.second)
        }
    }

    override fun deleteBetween(startId: Long, endId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            skinTempDaoWrapper.deleteBetween(startId, endId)
            Log.d(TAG, "Flush $key Data between $startId and $endId")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            skinTempDaoWrapper.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}