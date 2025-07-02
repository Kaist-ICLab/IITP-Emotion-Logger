package kaist.iclab.loggerstructure.daowrapper

import android.util.Log
import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.entity.EnvEntity

class EnvDaoWrapper(
    private val EnvDao: EnvDao
): DaoWrapper<EnvEntity> {
    companion object {
        private val TAG = this::class.simpleName
    }
    
    override suspend fun getAll(): List<EnvEntity> {
        return EnvDao.getAll()
    }

    override suspend fun insertEvent(entity: EnvEntity) {
        EnvDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<EnvEntity>) {
        EnvDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
        Log.d(TAG, "deleteAll() for Env Data")
        EnvDao.deleteAll()
    }

    override suspend fun getLast(): EnvEntity? {
        return EnvDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<EnvEntity>::class.java).toList()
        insertEvents(list)
    }
}