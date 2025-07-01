package kaist.iclab.wearablelogger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

enum class ScreenType {
    MAIN,
    BLUETOOTH_SCAN,
    SENSOR_STATUS,
}

@Composable
fun MainApp(
    mainViewModel: MainViewModel = koinViewModel(),
    bluetoothViewModel: BluetoothViewModel = koinViewModel()
) {
    val navController = rememberNavController()

    bluetoothViewModel.initBLEAdapter(LocalContext.current)

    MaterialTheme {
        NavHost(navController = navController, startDestination = ScreenType.MAIN.name) {
            composable(ScreenType.MAIN.name) { MainScreen(navController) }
            composable(ScreenType.SENSOR_STATUS.name) { StatusScreen(mainViewModel) }
            composable(ScreenType.BLUETOOTH_SCAN.name) { BluetoothScanScreen(bluetoothViewModel) }
        }
    }
}

@Composable
fun MainScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate(ScreenType.BLUETOOTH_SCAN.name) }) {
            Text("Scan for sensor")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate(ScreenType.SENSOR_STATUS.name) }) {
            Text("View Sensor Status")
        }
    }
}


