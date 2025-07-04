package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.entity.HREntity

class HRDaoWrapper(
    private val hrDao: HRDao
): DaoWrapper<HREntity> {
    companion object {
        private val TAG = HRDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(): Pair<Long, List<HREntity>> {
        val lastTimestamp = hrDao.getLast()?.timestamp ?: 0
        val entries = hrDao.getBefore(lastTimestamp)

        return Pair(lastTimestamp, entries)
    }

    override suspend fun getAll(): List<HREntity> {
        return hrDao.getAll()
    }

    override suspend fun insertEvent(entity: HREntity) {
        hrDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<HREntity>) {
        hrDao.insertEvents(entities)
    }

    override suspend fun deleteBefore(timestamp: Long) {
        hrDao.deleteBefore(timestamp)
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