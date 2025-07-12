package kaist.iclab.wearablelogger.ui

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kaist.iclab.wearablelogger.env.BluetoothScanner

private const val TAG = "BluetoothViewModel"

class BluetoothViewModel(
    private val bluetoothScanner: BluetoothScanner
) : ViewModel() {
    val scanResults = bluetoothScanner.scanResults
    val isScanning = bluetoothScanner.isScanning

    var bluSensorAddress by mutableStateOf("")
        private set

    fun startScan() {
        bluetoothScanner.startScan(bluSensorAddress)
    }

    fun stopScan() {
        bluetoothScanner.stopScan()
    }

    fun connectDevice(device: BluetoothDevice) {
        bluetoothScanner.connectDevice(device)
    }

    fun onBluSensorAddressChange(value: String) {
        bluSensorAddress = value
    }
}