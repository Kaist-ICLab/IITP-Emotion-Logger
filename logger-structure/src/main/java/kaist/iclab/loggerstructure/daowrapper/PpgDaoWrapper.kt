package kaist.iclab.loggerstructure.daowrapper

import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.entity.PpgEntity

class PpgDaoWrapper(
    val ppgDao: PpgDao
): DaoWrapper<PpgEntity> {
    override suspend fun getAll(): List<PpgEntity> {
        return ppgDao.getAll()
    }

    override suspend fun insertEvent(entity: PpgEntity) {
        ppgDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<PpgEntity>) {
        ppgDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
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