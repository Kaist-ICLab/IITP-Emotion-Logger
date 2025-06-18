package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.entity.HREntity

private const val TAG = "HRDaoWrapper"

class HRDaoWrapper(
    private val hrDao: HRDao
): DaoWrapper<HREntity> {
    override suspend fun getAll(): List<HREntity> {
        return hrDao.getAll()
    }

    override suspend fun insertEvent(entity: HREntity) {
        hrDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<HREntity>) {
        hrDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for HR Data")
        hrDao.deleteAll()
    }

    override suspend fun getLast(): HREntity? {
        return hrDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<HREntity>::class.java).toList()
        insertEvents(list)
    }
}