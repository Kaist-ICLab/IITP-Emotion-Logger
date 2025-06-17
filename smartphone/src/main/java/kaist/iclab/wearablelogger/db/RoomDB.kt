package kaist.iclab.wearablelogger.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.dao.HRDao
import kaist.iclab.loggerstructure.dao.PpgDao
import kaist.iclab.loggerstructure.dao.SkinTempDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.util.Converter
import kaist.iclab.loggerstructure.dao.StepDao
import kaist.iclab.loggerstructure.entity.StepEntity


@Database(
    version = 16,
    entities = [
        EventEntity::class,
        RecentEntity::class,
        PpgEntity::class,
        AccEntity::class,
        HREntity::class,
        SkinTempEntity::class,
        StepEntity::class,
    ],
    exportSchema = false,
)

@TypeConverters(Converter::class)
abstract class RoomDB : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun recentDao(): RecentDao
    abstract fun ppgDao(): PpgDao
    abstract fun accDao(): AccDao
    abstract fun hrDao(): HRDao
    abstract fun skinTempDao(): SkinTempDao
    abstract fun stepDao(): StepDao
}