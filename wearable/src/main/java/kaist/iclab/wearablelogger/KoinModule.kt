package kaist.iclab.wearablelogger

import androidx.room.Room
import kaist.iclab.loggerstructure.daowrapper.AccDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.HRDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.PpgDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.SkinTempDaoWrapper
import kaist.iclab.wearablelogger.collector.AccCollector
import kaist.iclab.wearablelogger.collector.HRCollector
import kaist.iclab.wearablelogger.collector.PpgCollector
import kaist.iclab.wearablelogger.collector.SkinTempCollector
import kaist.iclab.wearablelogger.collector.core.CollectorRepository
import kaist.iclab.wearablelogger.config.BatteryStateRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kaist.iclab.wearablelogger.ui.SettingsViewModel
import kaist.iclab.wearablelogger.uploader.UploaderRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val koinModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MyDataRoomDB::class.java,
            "MyDataRoomDB"
        )
            .fallbackToDestructiveMigration(true) // For Dev Phase!
            .build()
    }

    single {
        BatteryStateRepository()
    }

    single{
        ConfigRepository(androidContext())
    }

    single {
        HealthTrackerRepository(androidContext())
    }

    single {
        PpgCollector(androidContext(), get(), get(), get(), PpgDaoWrapper(get<MyDataRoomDB>().ppgDao()))
    }
    single {
        AccCollector(androidContext(), get(), get(), get(), AccDaoWrapper(get<MyDataRoomDB>().accDao()))
    }
    single {
        HRCollector(androidContext(), get(), get(), get(), HRDaoWrapper(get<MyDataRoomDB>().hrDao()))
    }
    single {
        SkinTempCollector(androidContext(), get(), get(), get(), SkinTempDaoWrapper(get<MyDataRoomDB>().skinTempDao()))
    }

    single(named("collectors")) {
        listOf(
            get<PpgCollector>(),
            get<AccCollector>(),
            get<HRCollector>(),
            get<SkinTempCollector>(),
        )
    }

    single {
        UploaderRepository(
            context = androidContext(),
            db = get()
        )
    }

    single {
        CollectorRepository(
            get(qualifier = qualifier("collectors")),
            get(),
            androidContext()
        )
    }

    viewModel {
        SettingsViewModel(get(), get())
    }

}