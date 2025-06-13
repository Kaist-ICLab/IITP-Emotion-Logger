package kaist.iclab.wearablelogger

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import kaist.iclab.wearablelogger.collector.acceleration.AccDao
import kaist.iclab.wearablelogger.collector.acceleration.AccEntity
import kaist.iclab.wearablelogger.collector.heartRate.HRDao
import kaist.iclab.wearablelogger.collector.heartRate.HREntity
import kaist.iclab.wearablelogger.collector.ppgGreen.PpgDao
import kaist.iclab.wearablelogger.collector.ppgGreen.PpgEntity
import kaist.iclab.wearablelogger.collector.skinTemp.SkinTempDao
import kaist.iclab.wearablelogger.collector.skinTemp.SkinTempEntity
import kaist.iclab.wearablelogger.collector.test.TestDao
import kaist.iclab.wearablelogger.collector.test.TestEntity

@Database(
    version = 15,
    entities = [
        TestEntity::class,
        PpgEntity::class,
        AccEntity::class,
        HREntity::class,
        SkinTempEntity::class,
    ],
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MyDataRoomDB:RoomDatabase() {
    abstract fun testDao(): TestDao
    abstract fun ppgDao(): PpgDao
    abstract fun accDao(): AccDao
    abstract fun hrDao(): HRDao
    abstract fun skinTempDao(): SkinTempDao
}

class Converters {
    @TypeConverter
    fun listToJson(value: List<Int>) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value,Array<Int>::class.java).toList()
}