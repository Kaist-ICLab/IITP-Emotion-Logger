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
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "PpgCollector"

class PpgCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    private val configRepository: ConfigRepository,
    private val ppgDao: PpgDao,
): HealthTrackerCollector(context) {
    override val key = CollectorType.PPG.name

    override val trackerEventListener = object : AbstractTrackerEventListener() {
        override fun onDataReceived(data: List<DataPoint>) {
            val dataReceived = System.currentTimeMillis()
            val ppgData = data.map{
                PpgEntity(
                    dataReceived = dataReceived,
                    timestamp = it.timestamp,
                    ppg = it.getValue(ValueKey.PpgSet.PPG_GREEN), //ADC value: might require DAC
                    status = it.getValue(ValueKey.PpgSet.GREEN_STATUS)
                )
            }
            CoroutineScope(Dispatchers.IO).launch {
                ppgDao.insertEvents(ppgData)
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

    override suspend fun stringifyData():String{
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return gson.toJson(ppgDao.getAll())
    }
    override fun flush() {
        Log.d(TAG, "Flush PPG Data")
        CoroutineScope(Dispatchers.IO).launch {
            ppgDao.deleteAll()
            Log.d(TAG, "deleteAll() for PPG Data")
        }
    }
}