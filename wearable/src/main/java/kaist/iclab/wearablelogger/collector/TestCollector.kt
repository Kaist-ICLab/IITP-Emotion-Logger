package kaist.iclab.wearablelogger.collector

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.dao.TestDao
import kaist.iclab.loggerstructure.entity.TestEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.loggerstructure.core.CollectorInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "TestCollector"

class TestCollector(
    val testDao: TestDao,
): CollectorInterface {
    override suspend fun getStatus(): Boolean {
        return false
    }
    
    override val key = CollectorType.TEST.name
    private var job: Job? = null

    override fun setup() {
        Log.d(TAG, "setup()")
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun startLogging() {
        Log.d(TAG, "startLogging()")
        if(job== null){
            job = CoroutineScope(Dispatchers.IO).launch {
                while(true){
                    delay(TimeUnit.SECONDS.toMillis(5))
                    testDao.insertEvent(TestEntity(timestamp = System.currentTimeMillis()))
                }
            }
        }
    }

    override fun stopLogging() {
        Log.d(TAG, "stopLogging()")
        job?.cancel()
        job = null
    }
    override suspend fun stringifyData():String{
        val gson = Gson()
//        return gson.toJson(mapOf(javaClass.simpleName to testDao.getAll()))
        return gson.toJson(testDao.getAll())
    }
    override fun flush() {
    }
}