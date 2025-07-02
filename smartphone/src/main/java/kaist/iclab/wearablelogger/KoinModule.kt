package kaist.iclab.wearablelogger

import androidx.room.Room
import kaist.iclab.loggerstructure.core.DaoWrapper
import kaist.iclab.loggerstructure.core.EntityBase
import kaist.iclab.loggerstructure.daowrapper.AccDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.EnvDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.HRDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.PpgDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.SkinTempDaoWrapper
import kaist.iclab.loggerstructure.daowrapper.StepDaoWrapper
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.step.StepCollector
import kaist.iclab.wearablelogger.ui.BluetoothViewModel
import kaist.iclab.wearablelogger.ui.MainViewModel
import kaist.iclab.wearablelogger.ui.StatusViewModel
import kaist.iclab.wearablelogger.util.DataReceiver
import kaist.iclab.wearablelogger.util.DataUploaderRepository
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

    // Dao
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
    single{
        get<RoomDB>().envDao()
    }

    // DaoWrapper
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
    single {
        EnvDaoWrapper(get<RoomDB>().envDao())
    }

    // Others
    single{
        StepCollector(androidContext(),get<RoomDB>().stepDao())
    }

    single {
        DataUploaderRepository(
            androidContext(),
            recentDao = get<RoomDB>().recentDao(),
            stepDao = get<RoomDB>().stepDao(),
            envDao = get<RoomDB>().envDao(),
            dataDao = mapOf(
                CollectorType.ACC.name to get<AccDaoWrapper>(),
                CollectorType.PPG.name to get<PpgDaoWrapper>(),
                CollectorType.HR.name to get<HRDaoWrapper>(),
                CollectorType.SKINTEMP.name to get<SkinTempDaoWrapper>(),
                CollectorType.ENV.name to get<EnvDaoWrapper>(),
                CollectorType.STEP.name to get<StepDaoWrapper>(),
            ) as Map<String, DaoWrapper<EntityBase>>,
        )
    }

    single{
        DataReceiver(
            androidContext(),
            get(),
            mapOf(
                CollectorType.ACC.name to get<AccDaoWrapper>(),
                CollectorType.PPG.name to get<PpgDaoWrapper>(),
                CollectorType.HR.name to get<HRDaoWrapper>(),
                CollectorType.SKINTEMP.name to get<SkinTempDaoWrapper>()
            ) as Map<String, DaoWrapper<EntityBase>>,
            get<DataUploaderRepository>()
        )
    }

    // ViewModel
    viewModel {
        StatusViewModel(
            get<RoomDB>().stepDao(), get<RoomDB>().envDao(), get<RoomDB>().recentDao(), listOf(
                get<AccDaoWrapper>(),
                get<PpgDaoWrapper>(),
                get<HRDaoWrapper>(),
                get<SkinTempDaoWrapper>(),
            ) as List<DaoWrapper<EntityBase>>
        )
    }

    viewModel {
        BluetoothViewModel()
    }

    viewModel {
        MainViewModel()
    }
}