package kaist.iclab.wearablelogger.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

@Composable
fun BluetoothScanScreen(
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isScanning = bluetoothViewModel.isScanning
    val bluetoothList = bluetoothViewModel.scanResults

    val enableBLELauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // BLE enabled
            Log.d("BLE", "Bluetooth enabled")
            bluetoothViewModel.startScan(context)
        } else {
            // User denied
            Log.d("BLE", "Bluetooth not enabled")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextField(
                value = bluetoothViewModel.bluSensorAddress,
                onValueChange = { it -> bluetoothViewModel.changeBluSensorAddress(it) },
                label = { Text("Bluetooth Address") },
                placeholder = { Text("XX:XX:XX:XX:XX:XX") },
                modifier = Modifier.weight(1.0F)
            )

            Spacer(modifier = Modifier.width(8.dp))

            if(isScanning) {
                Button(
                    onClick = { bluetoothViewModel.stopScan(context) },
                    modifier = Modifier.wrapContentWidth()
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
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text("Scan")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        BluetoothDeviceList(bluetoothList)
    }
}

@Composable
fun BluetoothDeviceList(bluetoothList: Map<String, BluetoothDevice>) {
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

    val list = bluetoothList.entries.toList()
    LazyColumn {
        items(list) { entry ->
            val address = entry.key
            val device = entry.value
            BluetoothDeviceItem(name = device.name ?: "Unknown", address = address)
        }
    }
}

@Composable
fun BluetoothDeviceItem(name: String, address: String) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
        Text(text = name, style = MaterialTheme.typography.bodyLarge)
        Text(text = address, style = MaterialTheme.typography.bodySmall)
        Modifier.padding(top = 8.dp)
        HorizontalDivider(
            modifier = Modifier,
            thickness = 1.dp,
        )
    }
}