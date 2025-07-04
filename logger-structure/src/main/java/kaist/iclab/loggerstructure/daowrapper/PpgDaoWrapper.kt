package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.entity.PpgEntity

class PpgDaoWrapper(
    private val ppgDao: PpgDao
): DaoWrapper<PpgEntity> {
    companion object {
        private val TAG = PpgDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(): Pair<Long, List<PpgEntity>> {
        val lastTimestamp = ppgDao.getLast()?.timestamp ?: 0
        val entries = ppgDao.getBefore(lastTimestamp)

        return Pair(lastTimestamp, entries)
    }

    override suspend fun getAll(): List<PpgEntity> {
        return ppgDao.getAll()
    }

    override suspend fun insertEvent(entity: PpgEntity) {
        ppgDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<PpgEntity>) {
        ppgDao.insertEvents(entities)
    }

    override suspend fun deleteBefore(timestamp: Long) {
        ppgDao.deleteBefore(timestamp)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for PPG Data")
        ppgDao.deleteAll()
    }

    override suspend fun getLast(): PpgEntity? {
        return ppgDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<PpgEntity>::class.java).toList()
        insertEvents(list)
    }
}