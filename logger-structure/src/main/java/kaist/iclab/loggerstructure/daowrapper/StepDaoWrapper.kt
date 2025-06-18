package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.StepEntity

private const val TAG = "StepDaoWrapper"

class StepDaoWrapper(
    private val stepDao: StepDao
): DaoWrapper<StepEntity> {
    override suspend fun getAll(): List<StepEntity> {
        return stepDao.getAll()
    }

    override suspend fun insertEvent(entity: StepEntity) {
        stepDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<StepEntity>) {
        stepDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Step Data")
        stepDao.deleteAll()
    }

    override suspend fun getLast(): StepEntity? {
        return stepDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<StepEntity>::class.java).toList()
        insertEvents(list)
    }
}