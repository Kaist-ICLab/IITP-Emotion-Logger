package kaist.iclab.wearablelogger

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kaist.iclab.loggerstructure.dao.AccDao
import kaist.iclab.loggerstructure.dao.RecordDao
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.RecordEntity
import kaist.iclab.loggerstructure.util.Converter

@Database(
    version = 18,
    entities = [
        AccEntity::class,
        RecordEntity::class,
    ],
    exportSchema = false,
)
@TypeConverters(Converter::class)
abstract class MyDataRoomDB:RoomDatabase() {
    abstract fun accDao(): AccDao
    abstract fun recordDao(): RecordDao
}