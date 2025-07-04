package kaist.iclab.wearablelogger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kaist.iclab.wearablelogger.R
import org.koin.androidx.compose.koinViewModel

enum class ScreenType {
    MAIN,
    BLUETOOTH_SCAN,
    SENSOR_STATUS,
}

@Composable
fun MainApp(
    mainViewModel: MainViewModel,
    statusViewModel: StatusViewModel = koinViewModel(),
    bluetoothViewModel: BluetoothViewModel = koinViewModel()
) {
    val navController = rememberNavController()

    bluetoothViewModel.initBLEAdapter(LocalContext.current)

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
                            mainViewModel = mainViewModel,
                            navController = navController
                        )
                    }
                    composable(ScreenType.SENSOR_STATUS.name) { StatusScreen(statusViewModel) }
                    composable(ScreenType.BLUETOOTH_SCAN.name) { BluetoothScanScreen(bluetoothViewModel) }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bluetoothDeviceAddress = mainViewModel.bluetoothDeviceAddress.collectAsState("None").value
    val wearables = mainViewModel.wearables.collectAsState(listOf()).value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("DEVICE INFO", style = MaterialTheme.typography.titleMedium)

                Text("Device ID: ${mainViewModel.deviceId}")
                Text("BluSensor Address: $bluetoothDeviceAddress")
                Text("Wearables: $wearables")
            }
        }

        Button(
            onClick = { navController.navigate(ScreenType.BLUETOOTH_SCAN.name) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.scan_for_blusensor))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate(ScreenType.SENSOR_STATUS.name) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Galaxy Watch Data")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Button(
                onClick = { mainViewModel.toggleEnvRunning(context) },
                modifier = Modifier.weight(1.0F)
            ) {
                if(!mainViewModel.isEnvRunning) Text("Run env Sensor")
                else Text("Stop env Sensor")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { mainViewModel.toggleStepRunning(context) },
                modifier = Modifier.weight(1.0F)
            ) {
                if(!mainViewModel.isStepRunning) Text("Run step Sensor")
                else Text("Stop step Sensor")
            }
        }

    }
}
