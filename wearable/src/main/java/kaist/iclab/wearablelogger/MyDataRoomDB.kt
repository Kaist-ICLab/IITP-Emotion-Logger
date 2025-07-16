package kaist.iclab.wearablelogger

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.dao.TestDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.entity.TestEntity
import kaist.iclab.loggerstructure.util.Converter

@Database(
    version = 17,
    entities = [
        TestEntity::class,
        PpgEntity::class,
        AccEntity::class,
        HREntity::class,
        SkinTempEntity::class,
    ],
    exportSchema = false,
)
@TypeConverters(Converter::class)
abstract class MyDataRoomDB:RoomDatabase() {
    abstract fun testDao(): TestDao
    abstract fun ppgDao(): PpgDao
    abstract fun accDao(): AccDao
    abstract fun hrDao(): HRDao
    abstract fun skinTempDao(): SkinTempDao
}