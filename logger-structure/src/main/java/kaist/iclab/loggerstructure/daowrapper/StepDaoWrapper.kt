package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.StepEntity

class StepDaoWrapper(
    private val StepDao: StepDao
): DaoWrapper<StepEntity> {
    companion object {
        private val TAG = this::class.simpleName
    }
    
    override suspend fun getAll(): List<StepEntity> {
        return StepDao.getAll()
    }

    override suspend fun insertEvent(entity: StepEntity) {
        StepDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<StepEntity>) {
        StepDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Step Data")
        StepDao.deleteAll()
    }

    override suspend fun getLast(): StepEntity? {
        return StepDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<StepEntity>::class.java).toList()
        insertEvents(list)
    }
}