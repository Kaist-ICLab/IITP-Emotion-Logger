package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.entity.AccEntity

private const val TAG = "AccDaoWrapper"

class AccDaoWrapper(
    private val accDao: AccDao
): DaoWrapper<AccEntity> {
    override suspend fun getAll(): List<AccEntity> {
        return accDao.getAll()
    }

    override suspend fun insertEvent(entity: AccEntity) {
        accDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<AccEntity>) {
        accDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for ACC Data")
        accDao.deleteAll()
    }

    override suspend fun getLast(): AccEntity? {
        return accDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<AccEntity>::class.java).toList()
        insertEvents(list)
    }
}