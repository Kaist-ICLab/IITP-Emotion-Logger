package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.PpgType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.BatteryStateReceiver
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PpgCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val configRepository: ConfigRepository,
    private val ppgDao: PpgDao,
): HealthTrackerCollector(context) {
    companion object {
        private val TAG = PpgCollector::class.simpleName
    }

    override val key = CollectorType.PPG.name

    override val trackerEventListener = object : AbstractTrackerEventListener() {
        override fun onDataReceived(data: List<DataPoint>) {
            val dataReceived = System.currentTimeMillis()
            Log.d(TAG, "$dataReceived, ${data.size}")
            val ppgEntities = data.map{
                PpgEntity(
                    dataReceived = dataReceived,
                    timestamp = it.timestamp,
                    ppg = it.getValue(ValueKey.PpgSet.PPG_GREEN), //ADC value: might require DAC
                    status = it.getValue(ValueKey.PpgSet.GREEN_STATUS)
                )
            }.filter {
                (!BatteryStateReceiver.isCharging || it.timestamp <= BatteryStateReceiver.chargeStartTimestamp)
            }

            Log.d(TAG, "insert ${ppgEntities.size} entities")

            CoroutineScope(Dispatchers.IO).launch {
                ppgDao.insertEvents(ppgEntities)
            }
        }
    }

    override fun initHealthTracker() {
        tracker = healthTrackerRepository.healthTrackingService
            .getHealthTracker(HealthTrackerType.PPG_CONTINUOUS, setOf(PpgType.GREEN))
        Log.v(TAG, tracker.toString())
    }

    override suspend fun getStatus(): Boolean {
        return configRepository.getSensorStatus("PPG Green")
    }

    override suspend fun stringifyData(): Pair<String, Long>{
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        val lastEntity = ppgDao.getLast()
        val lastTimestamp = lastEntity?.timestamp ?: 0L

        return Pair(gson.toJson(ppgDao.getBefore(lastTimestamp)), lastTimestamp)
    }

    override fun flushBefore(timestamp: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            ppgDao.deleteBefore(timestamp)
            Log.d(TAG, "Flush $TAG Data before $timestamp")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            ppgDao.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}