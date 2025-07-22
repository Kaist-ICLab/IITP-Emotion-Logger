package kaist.iclab.wearablelogger

import androidx.room.Room
import kaist.iclab.wearablelogger.collector.AccCollector
import kaist.iclab.wearablelogger.collector.core.CollectorRepository
import kaist.iclab.wearablelogger.collector.core.RecordRepository
import kaist.iclab.wearablelogger.config.ConfigRepository
import kaist.iclab.wearablelogger.healthtracker.HealthTrackerRepository
import kaist.iclab.wearablelogger.ui.SettingsViewModel
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
    single{
        ConfigRepository(androidContext())
    }
    single{
        RecordRepository(get<MyDataRoomDB>().recordDao())
    }

    single {
        HealthTrackerRepository(androidContext())
    }

    single {
        AccCollector(androidContext(), get(), get(), get<MyDataRoomDB>().accDao())
    }

    single(named("collectors")) {
        listOf(
            get<AccCollector>(),
        )
    }

    single {
        CollectorRepository(
            get(qualifier = qualifier("collectors")),
            androidContext()
        )
    }

    viewModel {
        SettingsViewModel(get(), get(), get())
    }

}