package kaist.iclab.wearablelogger.collector

import android.content.Context
import android.util.Log
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.collector.core.HealthTrackerCollector
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.AbstractTrackerEventListener
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AccCollector(
    context: Context,
    private val healthTrackerRepository: HealthTrackerRepository,
    configRepository: ConfigRepository,
    private val accDao: AccDao,
) : HealthTrackerCollector(context){
    companion object {
        private val TAG = AccCollector::class.simpleName
    }

    private val pid = configRepository.pidFlow
    private val label = configRepository.labelFlow

    override val key = CollectorType.ACC.name

    override val trackerEventListener: HealthTracker.TrackerEventListener = object :
        AbstractTrackerEventListener() {
        override fun onDataReceived(data: List<DataPoint>) {
            val dataReceived = System.currentTimeMillis()
            Log.d(TAG, "$dataReceived, ${data.size}")

            CoroutineScope(Dispatchers.IO).launch {
                val accEntities = data.map {
                    AccEntity(
                        timestamp = it.timestamp,
                        dataReceived = System.currentTimeMillis(),
                        pid = pid.first(),
                        label = label.first(),
                        x = convert2SIUnit(it.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X)),
                        y = convert2SIUnit(it.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y)),
                        z = convert2SIUnit(it.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z))
                    )
                }

                Log.d(TAG, "insert ${accEntities.size} entities")

                accDao.insertEvents(accEntities)
            }
        }

        override fun onFlushCompleted() {
            super.onFlushCompleted()
            tracker?.unsetEventListener()
        }

        private fun convert2SIUnit(value: Int): Float {
            return (9.81f / (16383.75f / 4.0f)) * value.toFloat()
        }
    }

    override fun initHealthTracker() {
        tracker = healthTrackerRepository.healthTrackingService
            .getHealthTracker(HealthTrackerType.ACCELEROMETER_CONTINUOUS)
        Log.d(TAG, "initHealthTracker: $tracker")
    }

    override fun deleteBetween(startId: Long, endId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            accDao.deleteBetween(startId, endId)
            Log.d(TAG, "Flush $key Data between $startId and $endId")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            accDao.deleteAll()
            Log.d(TAG, "Flush $TAG Data")
        }
    }
}