package kaist.iclab.wearablelogger

import androidx.room.Room
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.daowrapper.AccDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.HRDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.PpgDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.SkinTempDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.StepDaoWrapper
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.db.RoomDB
import kaist.iclab.wearablelogger.step.StepCollector
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            RoomDB::class.java,
            "RoomDB"
        )
            .fallbackToDestructiveMigration(true) // For Dev Phase!
            .build()
    }
    single{
        get<RoomDB>().eventDao()
    }
    single{
        get<RoomDB>().recentDao()
    }
    single{
        get<RoomDB>().accDao()
    }
    single{
        get<RoomDB>().ppgDao()
    }
    single{
        get<RoomDB>().hrDao()
    }
    single{
        get<RoomDB>().skinTempDao()
    }
    single{
        get<RoomDB>().stepDao()
    }

    single {
        AccDaoWrapper(get<RoomDB>().accDao())
    }

    single {
        PpgDaoWrapper(get<RoomDB>().ppgDao())
    }

    single {
        HRDaoWrapper(get<RoomDB>().hrDao())
    }

    single {
        SkinTempDaoWrapper(get<RoomDB>().skinTempDao())
    }

    single {
        StepDaoWrapper(get<RoomDB>().stepDao())
    }

    single{
        StepCollector(androidContext(),get<RoomDB>().stepDao())
    }

    single{
        DataReceiver(androidContext(), get(), mapOf(
            CollectorType.ACC.name to get<AccDaoWrapper>(),
            CollectorType.PPG.name to get<PpgDaoWrapper>(),
            CollectorType.HR.name to get<HRDaoWrapper>(),
            CollectorType.SKINTEMP.name to get<SkinTempDaoWrapper>()
        ) as Map<String, DaoWrapper<EntityBase>>)
    }

    viewModel {
        MainViewModel(get<RoomDB>().eventDao(), get<RoomDB>().stepDao(), get<RoomDB>().recentDao(), listOf(
            get<AccDaoWrapper>(),
            get<PpgDaoWrapper>(),
            get<HRDaoWrapper>(),
            get<SkinTempDaoWrapper>(),
            get<StepDaoWrapper>()
        ) as List<DaoWrapper<EntityBase>>)
    }
}