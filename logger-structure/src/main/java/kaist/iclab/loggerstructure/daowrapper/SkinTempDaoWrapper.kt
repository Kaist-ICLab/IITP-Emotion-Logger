package kaist.iclab.loggerstructure.daowrapper

import com.google.gson.Gson
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.entity.SkinTempEntity

class SkinTempDaoWrapper(
    val skinTempDao: SkinTempDao
): DaoWrapper<SkinTempEntity> {
    override suspend fun getAll(): List<SkinTempEntity> {
        return skinTempDao.getAll()
    }

    override suspend fun insertEvent(entity: SkinTempEntity) {
        skinTempDao.insertEvent(entity)
    }

    override suspend fun insertEvents(entities: List<SkinTempEntity>) {
        skinTempDao.insertEvents(entities)
    }

    override suspend fun deleteAll() {
        skinTempDao.deleteAll()
    }

    override suspend fun getLast(): SkinTempEntity {
        return skinTempDao.getLast()
    }

    override suspend fun insertEventsFromJson(json: String) {
        val list = Gson().fromJson(json, Array<SkinTempEntity>::class.java).toList()
        insertEvents(list)
    }
}