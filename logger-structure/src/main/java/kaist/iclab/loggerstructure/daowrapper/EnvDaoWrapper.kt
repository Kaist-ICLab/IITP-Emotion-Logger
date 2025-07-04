package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.entity.EnvEntity

class EnvDaoWrapper(
    private val envDao: EnvDao
): DaoWrapper<EnvEntity> {
    companion object {
        private val TAG = EnvDaoWrapper::class.simpleName
    }

    override suspend fun getBeforeLast(): Pair<Long, List<EnvEntity>> {
        val lastTimestamp = envDao.getLast()?.timestamp ?: 0
        val entries = envDao.getBefore(lastTimestamp)

        return Pair(lastTimestamp, entries)
    }
    
    override suspend fun getAll(): List<EnvEntity> {
        return envDao.getAll()
    }

    override suspend fun insertEvent(entity: EnvEntity) {
        envDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<EnvEntity>) {
        envDao.insertEvents(entities)
    }

    override suspend fun deleteBefore(id: Long) {
        envDao.deleteBefore(id)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Env Data")
        envDao.deleteAll()
    }

    override suspend fun getLast(): EnvEntity? {
        return envDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<EnvEntity>::class.java).toList()
        insertEvents(list)
    }
}