package kaist.iclab.wearablelogger.step

//import android.content.Context
//import android.util.Log
//import com.google.gson.GsonBuilder
//import com.samsung.android.sdk.health.data.HealthDataStore
//import com.samsung.android.sdk.health.data.error.PlatformInternalException
//import com.samsung.android.sdk.health.data.request.DataType
//import com.samsung.android.sdk.health.data.request.LocalTimeFilter
//import com.samsung.android.sdk.health.data.request.LocalTimeGroup
//import com.samsung.android.sdk.health.data.request.LocalTimeGroupUnit
//import com.samsung.android.sdk.health.data.request.Ordering
//import kaist.iclab.wearablelogger.collector.HealthDataCollector
//import kaist.iclab.wearablelogger.config.ConfigRepository
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.launch
//import java.lang.Thread.sleep
//import java.time.Instant
//import java.time.LocalDateTime
//import java.time.ZoneId
//import java.time.temporal.ChronoUnit
//
//class StepCollector(
//    context: Context,
//    private val configRepository: ConfigRepository,
//    private val stepDao: StepDAO
//): HealthDataCollector(context) {
//    override val TAG = javaClass.simpleName
//    private val syncPastLimitDays:Long = 64
//    private val syncUnitTimeMinutes:Long = 1
//
//    private var lastSynced:Long = System.currentTimeMillis() - syncPastLimitDays*24L*3600L*1000L
//
//    private suspend fun readAllDataByGroup(store: HealthDataStore, since: Long): Long {
//        //최근 걸음 정보를 불러와야 하기 때문에 분 단위로 내림한다.
//        //(워치 데이터는 실시간으로 휴대폰에 전송되지 않기 때문에 최소 1시간 여유를 두는 것)
//        val fromTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(since), ZoneId.systemDefault())
//            .truncatedTo(ChronoUnit.MINUTES).minusHours(1)
//        val req = DataType.StepsType
//            .TOTAL
//            .requestBuilder
//            .setLocalTimeFilterWithGroup(
//                LocalTimeFilter.since(
//                    fromTime
//                ),
//                LocalTimeGroup.of(LocalTimeGroupUnit.MINUTELY, 10)
//            )
//            .setOrdering(Ordering.DESC)
//            .build()
//        val resList = store.aggregateData(req).dataList
//        Log.d("StepCollector", "readAllDataByGroup() : ${resList.size} step data loaded, timeFilter=since(${fromTime})")
//
//        var maxTime:Long = since
//        resList.forEach{ it ->
//            stepDao.insertStepEvent(
//                StepEntity(
//                    dataReceived = System.currentTimeMillis(),
//                    startTime = it.startTime.toEpochMilli(),
//                    endTime = it.endTime.toEpochMilli(),
//                    step = it.value?:0L
//                )
//            )
//
//            if(it.endTime.toEpochMilli() > maxTime)
//                maxTime = it.endTime.toEpochMilli()
//        }
//
//        return maxTime
//    }
//
//    override suspend fun CoroutineScope.logData() {
//        val store = super.store!!
//
//        while(isActive){
//            val timestamp = System.currentTimeMillis()
//
//            //TODO : Insert only if this entity's timeslot is new. Else, do update instead of insertion.
//            try {
//                lastSynced = readAllDataByGroup(store, lastSynced)
//            } catch (e: PlatformInternalException) {
//                Log.e("StepCollector", "Sync Error at : $timestamp")
//                Log.e("StepCollector", Log.getStackTraceString(e))
//            }
//
//            sleep(1000)
//        }
//    }
//
//
//    override suspend fun getStatus(): Boolean {
//        return configRepository.getSensorStatus("Step")
//    }
//
//    override suspend fun stringifyData(): String {
//        val gson = GsonBuilder().setLenient().create()
//        return gson.toJson(mapOf(javaClass.simpleName to stepDao.getAll()))
//    }
//
//    override fun flush() {
//        Log.d(TAG, "Flush Step Data")
//        CoroutineScope(Dispatchers.IO).launch {
//            stepDao.deleteAll()
//            Log.d(TAG, "deleteAll() for Step Data")
//        }
//    }
//}