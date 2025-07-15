package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.PpgType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.daowrapper.PpgDaoWrapper
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.BatteryStateRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PpgCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val batteryStateRepository: BatteryStateRepository,
    private val configRepository: ConfigRepository,
    private val ppgDaoWrapper: PpgDaoWrapper,
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
                    ppgGreen = it.getValue(ValueKey.PpgSet.PPG_GREEN), //ADC value: might require DAC
                    ppgRed = it.getValue(ValueKey.PpgSet.PPG_RED), //ADC value: might require DAC
                    ppgIR = it.getValue(ValueKey.PpgSet.PPG_IR), //ADC value: might require DAC
                    status = it.getValue(ValueKey.PpgSet.GREEN_STATUS)
                )
            }.filter {
                (!batteryStateRepository.isCharging.value || it.timestamp <= batteryStateRepository.chargeStartTimestamp.value)
            }

            Log.d(TAG, "insert ${ppgEntities.size} entities")

            CoroutineScope(Dispatchers.IO).launch {
                ppgDaoWrapper.insertEvents(ppgEntities)
            }
        }
    }

    override fun initHealthTracker() {
        tracker = healthTrackerRepository.healthTrackingService
            .getHealthTracker(HealthTrackerType.PPG_CONTINUOUS, setOf(PpgType.GREEN, PpgType.IR, PpgType.RED))
        Log.v(TAG, tracker.toString())
    }

    override suspend fun getStatus(): Boolean {
        return configRepository.getSensorStatus("PPG")
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<String> {
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return ppgDaoWrapper.getBeforeLast(startId, limit).map { it ->
            gson.toJson(it.second)
        }
    }
    override fun deleteBetween(startId: Long, endId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            ppgDaoWrapper.deleteBetween(startId, endId)
            Log.d(TAG, "Flush $key Data between $startId and $endId")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            ppgDaoWrapper.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}