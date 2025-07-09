package kaist.iclab.wearablelogger.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kaist.iclab.loggerstructure.entity.AccEntity
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.loggerstructure.entity.HREntity
import kaist.iclab.loggerstructure.entity.PpgEntity
import kaist.iclab.loggerstructure.entity.SkinTempEntity
import kaist.iclab.loggerstructure.entity.StepEntity
import kaist.iclab.loggerstructure.entity.defaultAccEntity
import kaist.iclab.loggerstructure.entity.defaultEnvEntity
import kaist.iclab.loggerstructure.entity.defaultHREntity
import kaist.iclab.loggerstructure.entity.defaultPpgEntity
import kaist.iclab.loggerstructure.entity.defaultSkinTempEntity
import kaist.iclab.loggerstructure.entity.defaultStepEntity
import kaist.iclab.loggerstructure.util.CollectorType
import kaist.iclab.wearablelogger.R
import kaist.iclab.wearablelogger.util.TimeUtil
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

enum class ScreenType {
    MAIN,
    BLUETOOTH_SCAN,
    DEBUG,
}

@Composable
fun MainApp(
    mainViewModel: MainViewModel,
    bluetoothViewModel: BluetoothViewModel = koinViewModel(),
    debugViewModel: DebugViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    MaterialTheme {
        Scaffold {
            innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(navController = navController, startDestination = ScreenType.MAIN.name) {
                    composable(ScreenType.MAIN.name) {
                        MainScreen(
                            bluetoothDeviceAddress = mainViewModel.bluetoothDeviceAddress.collectAsState(
                                "None"
                            ).value,
                            wearable = mainViewModel.wearables.collectAsState(null).value,
                            deviceId = mainViewModel.deviceId,
                            isEnvRunning = mainViewModel.isEnvRunning.collectAsState().value,
                            isStepRunning = mainViewModel.isStepRunning.collectAsState().value,

                            currentTime = mainViewModel.currentTime,
                            recentTime = mainViewModel.recentTimestamp.collectAsState().value,
                            syncTime = mainViewModel.syncTime.collectAsState().value,
                            uploadTime = mainViewModel.uploadTime.collectAsState().value,

                            recentHREntity = mainViewModel.recentHREntity.collectAsState().value ?: defaultHREntity,
                            recentAccEntity = mainViewModel.recentAccEntity.collectAsState().value ?: defaultAccEntity,
                            recentPpgEntity = mainViewModel.recentPpgEntity.collectAsState().value ?: defaultPpgEntity,
                            recentSkinTempEntity = mainViewModel.recentSkinTempEntity.collectAsState().value ?: defaultSkinTempEntity,
                            recentStepEntity = mainViewModel.recentStepEntity.collectAsState().value ?: defaultStepEntity,
                            recentEnvEntity = mainViewModel.recentEnvEntity.collectAsState().value ?: defaultEnvEntity,

                            tickTime = { mainViewModel.tickTime() },
                            navigateToBluetoothScan = { navController.navigate(ScreenType.BLUETOOTH_SCAN.name) },
                            navigateToDebug = { navController.navigate(ScreenType.DEBUG.name) },
                            toggleEnvRunning = { mainViewModel.toggleEnvRunning(context) },
                            toggleStepRunning = { mainViewModel.toggleStepRunning(context)},
                        )
                    }
                    composable(ScreenType.BLUETOOTH_SCAN.name) { BluetoothScanScreen(bluetoothViewModel) }
                    composable(ScreenType.DEBUG.name) { DebugScreen(
                        uploadSingleStepEntity = { debugViewModel.uploadSingleStepEntity() },
                        flush = { debugViewModel.flush() }
                    ) }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    bluetoothDeviceAddress: String,
    wearable: String?,
    deviceId: String,
    isEnvRunning: Boolean,
    isStepRunning: Boolean,
    currentTime: Long,
    recentTime: Long,
    syncTime: Map<CollectorType, Long>,
    uploadTime: Map<CollectorType, Long>,
    recentHREntity: HREntity,
    recentAccEntity: AccEntity,
    recentPpgEntity: PpgEntity,
    recentSkinTempEntity: SkinTempEntity,
    recentStepEntity: StepEntity,
    recentEnvEntity: EnvEntity,
    tickTime: () -> Unit,
    navigateToBluetoothScan: () -> Unit,
    navigateToDebug: () -> Unit,
    toggleEnvRunning: () -> Unit,
    toggleStepRunning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        while (true) {
            tickTime()
            delay(1000L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DeviceInfo(
            deviceId = deviceId,
            bluetoothDeviceAddress = bluetoothDeviceAddress,
            wearable = wearable,
            navigateToDebug = navigateToDebug,
            navigateToBluetoothScan = navigateToBluetoothScan,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Row {
                        Text(
                            "Current Time: ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            TimeUtil.timestampToString(currentTime)
                        )
                    }
                    Row {
                        Text(
                            "Recent Entity Update: ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            TimeUtil.timestampToString(recentTime)
                        )
                    }
                }

            }
            Spacer(modifier = Modifier.height(8.dp))
            AccordionGroup(
                syncTime = syncTime,
                uploadTime = uploadTime,
                recentHREntity = recentHREntity,
                recentAccEntity = recentAccEntity,
                recentPpgEntity = recentPpgEntity,
                recentSkinTempEntity = recentSkinTempEntity,
                recentStepEntity = recentStepEntity,
                recentEnvEntity = recentEnvEntity,
                isStepRunning = isStepRunning,
                isEnvRunning = isEnvRunning,
                toggleStepRunning = toggleStepRunning,
                toggleEnvRunning = toggleEnvRunning,
            )
        }

    }
}

@Composable
fun DeviceInfo(
    deviceId: String,
    bluetoothDeviceAddress: String,
    wearable: String?,
    navigateToBluetoothScan: () -> Unit,
    navigateToDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Device Info Icon"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Device Info",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Device ID: $deviceId")
            Text("BluSensor Address: $bluetoothDeviceAddress")
            Text("Wearable: ${wearable ?: "None"}")
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = navigateToBluetoothScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.scan_for_blusensor))
            }

            Button(
                onClick = navigateToDebug,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Debug")
            }
        }
    }
}

@Composable
fun AccordionGroup(
    syncTime: Map<CollectorType, Long>,
    uploadTime: Map<CollectorType, Long>,
    recentHREntity: HREntity,
    recentAccEntity: AccEntity,
    recentPpgEntity: PpgEntity,
    recentSkinTempEntity: SkinTempEntity,
    recentStepEntity: StepEntity,
    recentEnvEntity: EnvEntity,
    isStepRunning: Boolean,
    isEnvRunning: Boolean,
    toggleStepRunning: () -> Unit,
    toggleEnvRunning: () -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded: MutableList<Boolean> = remember { mutableStateListOf(false, false, false, false, false, false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Accordion(
            imageVector = Icons.Default.Favorite,
            syncTime = syncTime[CollectorType.HR] ?: -1,
            uploadTime = uploadTime[CollectorType.HR] ?: -1,
            title = "Heart Rate",
            isExpanded = expanded[0],
            toggleExpansion = { expanded[0] = !expanded[0] }
        ) {
            Column {
                TagValueText("Data Received",
                    TimeUtil.timestampToString(recentHREntity.dataReceived)
                )
                TagValueText("Timestamp", TimeUtil.timestampToString(recentHREntity.timestamp))
                TagValueText("Heart Rate", "${recentHREntity.hr}")
                TagValueText("Heart Rate Status", "${recentHREntity.hrStatus}")
                TagValueText("IBI(Inter-beat interval)", "${recentHREntity.ibi}")
                TagValueText("IBI Status", "${recentHREntity.ibiStatus}")
            }
        }

        Accordion(
            imageVector = Icons.Default.Speed,
            syncTime = syncTime[CollectorType.ACC] ?: -1,
            uploadTime = uploadTime[CollectorType.ACC] ?: -1,
            title = "Acceleration",
            isExpanded = expanded[1],
            toggleExpansion = { expanded[1] = !expanded[1] }
        ) {
            Column {
                TagValueText("Data Received", TimeUtil.timestampToString(recentAccEntity.dataReceived))
                TagValueText("Timestamp", TimeUtil.timestampToString(recentAccEntity.dataReceived))
                TagValueText("Acceleration", "x = ${recentAccEntity.x}, y = ${recentAccEntity.y}, z = ${recentAccEntity.z}")
            }
        }

        Accordion(
            imageVector = Icons.Default.MonitorHeart,
            syncTime = syncTime[CollectorType.PPG] ?: -1,
            uploadTime = uploadTime[CollectorType.PPG] ?: -1,
            title = "PPG",
            isExpanded = expanded[2],
            toggleExpansion = { expanded[2] = !expanded[2] }
        ) {
            Column {
                TagValueText("Data Received", TimeUtil.timestampToString(recentPpgEntity.dataReceived))
                TagValueText("Timestamp", TimeUtil.timestampToString(recentPpgEntity.timestamp))
                TagValueText("PPG value", "${recentPpgEntity.ppg}")
                TagValueText("PPG status", "${recentPpgEntity.status}")
            }
        }

        Accordion(
            imageVector = Icons.Default.DeviceThermostat,
            syncTime = syncTime[CollectorType.SKINTEMP] ?: -1,
            uploadTime = uploadTime[CollectorType.SKINTEMP] ?: -1,
            title = "Skin Temperature",
            isExpanded = expanded[3],
            toggleExpansion = { expanded[3] = !expanded[3] }
        ) {
            Column {
                TagValueText("Data Received", TimeUtil.timestampToString(recentSkinTempEntity.dataReceived))
                TagValueText("Timestamp", TimeUtil.timestampToString(recentSkinTempEntity.timestamp))
                TagValueText("Ambient Temperature", "${recentSkinTempEntity.ambientTemp}")
                TagValueText("Skin Temperature", "${recentSkinTempEntity.objectTemp}")
                TagValueText("Status", "${recentSkinTempEntity.status}")
            }
        }

        Accordion(
            imageVector = Icons.AutoMirrored.Default.DirectionsRun,
            syncTime = null,
            uploadTime = uploadTime[CollectorType.STEP] ?: -1,
            title = "Steps",
            button = {
                Spacer(modifier = Modifier.width(4.dp))
                CollectionToggleButton(
                    isRunning = isStepRunning,
                    collectionType = "step",
                    toggleRunning = toggleStepRunning,
                )
            },
            isExpanded = expanded[4],
            toggleExpansion = { expanded[4] = !expanded[4] },
        ) {
            TagValueText("Data Received", TimeUtil.timestampToString(recentStepEntity.dataReceived))
            TagValueText("Time Slot", "${TimeUtil.timestampToString(recentStepEntity.startTime)} ~ ${TimeUtil.timestampToString(recentStepEntity.endTime)}")
            TagValueText("Steps", "${recentStepEntity.step}")
        }

        Accordion(
            imageVector = Icons.Default.Sensors,
            syncTime = null,
            uploadTime = uploadTime[CollectorType.ENV] ?: -1,
            title = "Environment",
            button = {
                Spacer(modifier = Modifier.width(4.dp))
                CollectionToggleButton(
                    isRunning = isEnvRunning,
                    collectionType = "environment",
                    toggleRunning = toggleEnvRunning,
                )
            },
            isExpanded = expanded[5],
            toggleExpansion = { expanded[5] = !expanded[5] }
        ) {
            TagValueText("Timestamp", TimeUtil.timestampToString(recentEnvEntity.timestamp))
            TagValueText("Temperature", "${recentEnvEntity.temperature}")
            TagValueText("Humidity", "${recentEnvEntity.humidity}")
            TagValueText("CO2", "${recentEnvEntity.co2}")
            TagValueText("tVOC", "${recentEnvEntity.tvoc}")
        }
    }
}

@Composable
fun TagValueText(
    tag: String,
    value: String
) {
    Row {
        Text(
            text = "$tag: ",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CollectionToggleButton(
    isRunning: Boolean,
    collectionType: String,
    toggleRunning: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = toggleRunning,
        modifier = modifier.size(24.dp)
            .clip(CircleShape)
            .padding(0.dp)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        if (!isRunning) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start $collectionType collection",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop $collectionType collection",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun DeviceInfoPreview() {
    MaterialTheme {
        DeviceInfo(
            bluetoothDeviceAddress = "XX:XX:XX:XX:XX:XX",
            wearable = "None",
            deviceId = "1234567890abcdef",
            navigateToDebug = {},
            navigateToBluetoothScan = {}
        )
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun AccordionGroupPreview() {
    MaterialTheme {
        AccordionGroup(
            syncTime = mapOf(
                CollectorType.HR to 1751722629000L,
            ),
            uploadTime = mapOf(),
            recentHREntity = defaultHREntity,
            recentAccEntity = defaultAccEntity,
            recentPpgEntity = defaultPpgEntity,
            recentSkinTempEntity = defaultSkinTempEntity,
            recentStepEntity = defaultStepEntity,
            recentEnvEntity = defaultEnvEntity,
            isStepRunning = true,
            isEnvRunning = true,
            toggleStepRunning = {},
            toggleEnvRunning = {}
        )
    }
}

@Preview(
    showBackground = true,
)
@Composable
fun MainScreenPreview() {
    val currentTime = System.currentTimeMillis()
    MaterialTheme {
        MainScreen(
            bluetoothDeviceAddress = "XX:XX:XX:XX:XX:XX",
            wearable = "None",
            deviceId = "1234567890abcdef",
            isEnvRunning = false,
            isStepRunning = false,
            currentTime =  currentTime,
            recentTime = currentTime,
            syncTime = mapOf(
                CollectorType.HR to currentTime,
                CollectorType.ACC to currentTime,
                CollectorType.PPG to currentTime,
                CollectorType.SKINTEMP to currentTime,
            ),
            uploadTime = mapOf(
                CollectorType.HR to currentTime,
                CollectorType.ACC to currentTime,
                CollectorType.PPG to currentTime,
                CollectorType.SKINTEMP to currentTime,
                CollectorType.STEP to currentTime,
                CollectorType.ENV to currentTime,
            ),
            recentHREntity = defaultHREntity,
            recentAccEntity = defaultAccEntity,
            recentPpgEntity = defaultPpgEntity,
            recentSkinTempEntity = defaultSkinTempEntity,
            recentStepEntity = defaultStepEntity,
            recentEnvEntity = defaultEnvEntity,
            tickTime = {},
            navigateToDebug = {},
            navigateToBluetoothScan = {},
            toggleEnvRunning = {},
            toggleStepRunning = {},
        )
    }
}
