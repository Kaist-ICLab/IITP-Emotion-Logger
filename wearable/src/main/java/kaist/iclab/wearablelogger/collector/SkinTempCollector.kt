package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SkinTempCollector"

class SkinTempCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val configRepository: ConfigRepository,
    private val skinTempDao: SkinTempDao
) : HealthTrackerCollector(context) {
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
            val skinTempData = data.map {
                SkinTempEntity(
                    dataReceived = dataReceived,
                    timestamp = it.timestamp,
                    ambientTemp = it.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE),
                    objectTemp = it.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE),
                    status = it.getValue(ValueKey.SkinTemperatureSet.STATUS)
                )
            }
            CoroutineScope(Dispatchers.IO).launch {
                skinTempDao.insertEvents(skinTempData)
            }
        }
    }
    override suspend fun stringifyData(): Pair<String, Long>{
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        val lastEntity = skinTempDao.getLast()
        val lastTimestamp = lastEntity?.timestamp ?: 0L

        return Pair(gson.toJson(skinTempDao.getBefore(lastTimestamp)), lastTimestamp)
    }


    override fun flushBefore(timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            skinTempDao.deleteBefore(timestamp)
            Log.d(TAG, "Flush $TAG Data before $timestamp")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            skinTempDao.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}