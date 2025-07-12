package kaist.iclab.wearablelogger.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kaist.iclab.wearablelogger.env.BluetoothConnectStatus

@Composable
fun BluetoothScanScreen(
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val isScanning by bluetoothViewModel.isScanning.collectAsState()
    val bluetoothList by bluetoothViewModel.scanResults.collectAsState()

    val enableBLELauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // BLE enabled
            Log.d("BLE", "Bluetooth enabled")
            bluetoothViewModel.startScan()
        } else {
            // User denied
            Log.d("BLE", "Bluetooth not enabled")
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = bluetoothViewModel.bluSensorAddress,
                onValueChange = { bluetoothViewModel.onBluSensorAddressChange(it) },
                label = { Text("Bluetooth Address", style = MaterialTheme.typography.labelSmall) },
                placeholder = { Text("XX:XX:XX:XX:XX:XX", style = MaterialTheme.typography.bodySmall) },
                textStyle = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .weight(1.0F),
            )

            Spacer(modifier = Modifier.width(8.dp))

            if(isScanning) {
                Button(
                    onClick = { bluetoothViewModel.stopScan() },
                    modifier = Modifier.width(90.dp)
                ) {
                    Text(
                        "Stop",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Button(
                    onClick = {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        enableBLELauncher.launch(intent)
                    },
                    modifier = Modifier.width(90.dp)
                ) {
                    Text("Scan")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BluetoothDeviceList(
            bluetoothList = bluetoothList,
            connectDevice = { device -> bluetoothViewModel.connectDevice(device) }
        )
    }
}

@Composable
fun BluetoothDeviceList(
    bluetoothList: Map<BluetoothDevice, BluetoothConnectStatus>,
    connectDevice: (device: BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    if (ActivityCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return
    }

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        items(bluetoothList.entries.toList()) { entry ->
            val device = entry.key
            val connectStatus = entry.value
            BluetoothDeviceItem(
                name = device.name ?: "Unknown",
                address = device.address,
                connectStatus = connectStatus,
                onClick = { connectDevice(device) }
            )
        }
    }
}

@Composable
fun BluetoothDeviceItem(
    name: String,
    address: String,
    connectStatus: BluetoothConnectStatus,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(connectStatus) {
        val message = when(connectStatus){
            BluetoothConnectStatus.ONGOING -> "Trying to connect to device..."
            BluetoothConnectStatus.FAIL -> "Connection Failed"
            BluetoothConnectStatus.SUCCESS -> "Connection successful"
            else -> ""
        }

        if(connectStatus != BluetoothConnectStatus.IDLE) Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    Card(modifier = Modifier
        .fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            Text(text = address, style = MaterialTheme.typography.bodySmall)
        }
    }
}