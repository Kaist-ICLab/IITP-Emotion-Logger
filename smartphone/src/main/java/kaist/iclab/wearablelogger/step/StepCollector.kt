package kaist.iclab.wearablelogger.step

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.error.PlatformInternalException
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.LocalTimeFilter
import com.samsung.android.sdk.health.data.request.LocalTimeGroup
import com.samsung.android.sdk.health.data.request.LocalTimeGroupUnit
import com.samsung.android.sdk.health.data.request.Ordering
import kaist.iclab.loggerstructure.daowrapper.StepDaoWrapper
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val TAG = "StepCollector"

class StepCollector(
    context: Context,
    private val stepDaoWrapper: StepDaoWrapper,
): HealthDataCollector(context) {
    override val key = CollectorType.STEP.name
    // Start from 64 days before
    private val syncPastLimitDays:Long = 7
//    private val syncUnitTimeMinutes:Long = 1

    private var lastSynced:Long = System.currentTimeMillis() - syncPastLimitDays*24L*3600L*1000L

    private suspend fun readAllDataByGroup(store: HealthDataStore, since: Long): Long {
        // We want to collect recent steps, so grouped in several minutes unit
        // The wearable's data doesn't sync instantly to mobile devices, so have 1 hour of margin to load the steps
        val tenMinuteDigit = (since % (60 * 60 * 1000)) / (60 * 1000) / 10 * 10
        val fromTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(since), ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.MINUTES)
            .withMinute(tenMinuteDigit.toInt())
            .minusHours(1)
        val req = DataType.StepsType
            .TOTAL
            .requestBuilder
            .setLocalTimeFilterWithGroup(
                LocalTimeFilter.since(
                    fromTime
                ),
                LocalTimeGroup.of(LocalTimeGroupUnit.MINUTELY, 10)
            )
            .setOrdering(Ordering.DESC)
            .build()
        val resList = store.aggregateData(req).dataList
        Log.d("StepCollector", "readAllDataByGroup() : ${resList.size} step data loaded, timeFilter=since(${fromTime})")

        var maxTime:Long = since
        resList.forEach{ it ->
            stepDaoWrapper.upsertEvent(
                StepEntity(
                    dataReceived = System.currentTimeMillis(),
                    startTime = it.startTime.toEpochMilli(),
                    endTime = it.endTime.toEpochMilli(),
                    step = it.value ?: 0L
                )
            )

            if(it.endTime.toEpochMilli() > maxTime)
                maxTime = it.endTime.toEpochMilli()
        }

        return maxTime
    }

    override suspend fun CoroutineScope.logData() {
        Log.v(TAG, "Logging step data...")
        val store = super.store!!
        val timestamp = System.currentTimeMillis()

        try {
            lastSynced = readAllDataByGroup(store, lastSynced)
        } catch (e: PlatformInternalException) {
            Log.e(TAG, "Sync Error at : $timestamp")
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    override suspend fun getStatus(): Boolean {
        // TODO: Set up proper getStatus() function
        return super.isAvailable()
//        return configRepository.getSensorStatus("Step")
    }

    override suspend fun getBeforeLast(startId: Long, limit: Long): Sequence<String> {
        val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
        return stepDaoWrapper.getBeforeLast(startId, limit).map { it ->
            gson.toJson(it.second)
        }
    }

    override fun deleteBetween(startId: Long, endId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            stepDaoWrapper.deleteBetween(startId, endId)
            Log.d(TAG, "Flush $key Data between $startId and $endId")
        }
    }

    override fun flush() {
        CoroutineScope(Dispatchers.IO).launch {
            stepDaoWrapper.deleteAll()
            Log.d(TAG, "Flush Step Data")
        }
    }
}