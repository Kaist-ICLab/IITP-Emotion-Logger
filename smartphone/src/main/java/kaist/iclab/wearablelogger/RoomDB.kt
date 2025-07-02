package kaist.iclab.wearablelogger

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.util.Converter
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.dao.RecentDao
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.entity.RecentEntity

@Database(
    version = 20,
    entities = [
        RecentEntity::class,
        PpgEntity::class,
        AccEntity::class,
        HREntity::class,
        SkinTempEntity::class,
        StepEntity::class,
        EnvEntity::class,
    ],
    exportSchema = false,
)

@TypeConverters(Converter::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun recentDao(): RecentDao
    abstract fun ppgDao(): PpgDao
    abstract fun accDao(): AccDao
    abstract fun hrDao(): HRDao
    abstract fun skinTempDao(): SkinTempDao
    abstract fun stepDao(): StepDao
    abstract fun envDao(): EnvDao
}