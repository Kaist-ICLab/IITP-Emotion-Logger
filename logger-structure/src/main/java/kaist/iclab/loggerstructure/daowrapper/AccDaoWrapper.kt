package kaist.iclab.loggerstructure.daowrapper

import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.entity.AccEntity

class AccDaoWrapper(
    val accDao: AccDao
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
        accDao.deleteAll()
    }

    override suspend fun getLast(): AccEntity {
        return accDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<AccEntity>::class.java).toList()
        insertEvents(list)
    }
}