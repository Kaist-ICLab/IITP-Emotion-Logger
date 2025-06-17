package kaist.iclab.loggerstructure.daowrapper

import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.entity.HREntity

class HRDaoWrapper(
    val hrDao: HRDao
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
        hrDao.deleteAll()
    }

    override suspend fun getLast(): HREntity {
        return hrDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<HREntity>::class.java).toList()
        insertEvents(list)
    }
}