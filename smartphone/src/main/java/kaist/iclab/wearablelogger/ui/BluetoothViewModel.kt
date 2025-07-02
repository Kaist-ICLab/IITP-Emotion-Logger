package kaist.iclab.wearablelogger.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import kaist.iclab.wearablelogger.blu.BluetoothHelper.SCAN_PERIOD
import kaist.iclab.wearablelogger.blu.BluetoothHelper.UUID_SERVICE
import kaist.iclab.wearablelogger.blu.BluetoothHelper.findBLECharacteristics
import kaist.iclab.wearablelogger.blu.SharedPreferencesUtil

private const val TAG = "BluetoothViewModel"

class BluetoothViewModel() : ViewModel() {
    // flag for scanning
    var isScanning by mutableStateOf(false)
        private set

    // scan results
    var scanResults = mutableStateMapOf<String, BluetoothDevice>()
        private set

    var bluSensorAddress by mutableStateOf("")
        private set

    var deviceAddress: String? = null
        private set

    // ble adapter
    private var bleAdapter: BluetoothAdapter? = null

    // scan callback
    private var scanCallback: ScanCallback? = null
    // ble scanner
    private var bleScanner: BluetoothLeScanner? = null
    // scan handler
    private var scanHandler: Handler? = null
    private var sharedPreferencesUtil: SharedPreferencesUtil? = null

    // BLE connected Gatt
    private var bleGatt: BluetoothGatt? = null

    fun initBLEAdapter(context: Context) {
        val bleManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleAdapter = bleManager.adapter
    }

    fun changeBluSensorAddress(value: String) {
        bluSensorAddress = value
    }

    fun startScan(context: Context) {
        disconnectGattServer(context)

        // check ble adapter and ble enabled
        if (bleAdapter == null || !bleAdapter!!.isEnabled) {
            Log.w(TAG, "Scanning Failed: ble not enabled")
            return
        }

        sharedPreferencesUtil = SharedPreferencesUtil.getInstance(context)

        // setup scan filters
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        val filters: MutableList<ScanFilter?> = ArrayList<ScanFilter?>()
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID_SERVICE))
            .build()
        filters.add(scanFilter)

        scanResults = mutableStateMapOf()
        scanCallback = BLEScanCallback(scanResults as MutableMap<String, BluetoothDevice>)

        // Ready to scan
        // start scan
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
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

        if(bleScanner == null) bleScanner = bleAdapter?.bluetoothLeScanner
        bleScanner?.startScan(filters, settings, scanCallback)
        // set scanning flag
        isScanning = true
        Log.v(TAG, "set isScanning to True")

        // Handler for stop scanning
        scanHandler = Handler(Looper.getMainLooper())
        scanHandler!!.postDelayed(Runnable { this.stopScan(context) }, SCAN_PERIOD.toLong())
    }

    /* Stop scanning */
    fun stopScan(context: Context) {
        Log.d(TAG, "stopScan()")
        // check pre-conditions
        Log.d(TAG, "isScanning: $isScanning, bleAdapter: $bleAdapter, isEnabled?: ${bleAdapter!!.isEnabled}, bleScanner: $bleScanner")
        if (isScanning && bleAdapter != null && bleAdapter!!.isEnabled && bleScanner != null) {
            // stop scanning
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Log.d(TAG, "no permission for bluetooth scan")
                return
            }
            Log.d(TAG, "Stop scan")
            Log.d(TAG, "Manifest.permission.BLUETOOTH " + Manifest.permission.BLUETOOTH.javaClass.getName()
            )
            bleScanner!!.stopScan(scanCallback)
            scanComplete(context)
        }
        // reset flags
        Log.d(TAG, "Reset flags")
        scanCallback = null
        isScanning = false
        scanHandler = null
    }

    /* Handle scan results after scan stopped */
    private fun scanComplete(context: Context) {
        // check if nothing found
        if (scanResults.isEmpty()) {
            Log.d(TAG, "scan result is empty")
            return
        }
        // loop over the scan results and connect to them
        for (deviceAddress in scanResults.keys) {
            Log.d(TAG, "Found device: $deviceAddress")

            // get device instance using its MAC address
            val device = scanResults[deviceAddress]
            Log.d(TAG, "connecting device: $deviceAddress")

            // connect to the device
            if ((bluSensorAddress != deviceAddress) and (bluSensorAddress != "")) {
                Log.d(TAG, "Filter out the device whose address: $deviceAddress")
                scanResults.remove(deviceAddress)
                continue
            }
            connectDevice(device!!, context)
        }
    }

    private fun connectDevice(device: BluetoothDevice, context: Context) {
        val gattClientCallback = GattClientCallback(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        deviceAddress = device.getAddress()
        Log.d(TAG, "connect to: $deviceAddress")


        bleGatt = device.connectGatt(context, true, gattClientCallback)
    }

    /* BLE Scan Callback class */
    private inner class BLEScanCallback (private val callbackScanResults: MutableMap<String, BluetoothDevice>) : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(TAG, "onScanResult")
            addScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(error: Int) {
            Log.e(TAG, "BLE scan failed with code $error")
        }

        /* Add scan result */
        fun addScanResult(result: ScanResult) {
            // get scanned device
            val device = result.device
            // get scanned device MAC address
            val deviceAddress = device.getAddress()
            // add the device to the result list
            callbackScanResults.put(deviceAddress, device)
        }
    }

    /* Disconnect Gatt Server */
    private fun disconnectGattServer(context: Context) {
        Log.d(TAG, "Closing Gatt connection")

        if(bleGatt == null) return

        // disconnect and close the gatt
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        bleGatt!!.disconnect()
        bleGatt!!.close()
    }

    /* Gatt Client Callback class */
    private inner class GattClientCallback(val context: Context) : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer(context)
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer(context)
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // update the connection status message
//                getActivity()!!.runOnUiThread(object : Runnable {
//                    override fun run() {
//                        // Stuff that updates the UI
////                        updateConnectionState(true);
//                        tv_status_!!.setText("Connected")
//                        (getActivity() as BluetoothActivity).startDataCollection()
//                    }
//                })

                Log.d(TAG, "Connected to the GATT server")
                if (ActivityCompat.checkSelfPermission(
                        context,
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
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer(context)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            // check if the discovery failed
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Device service discovery failed, status: $status")
                return
            }
            // find discovered characteristics
            val matchingCharacteristics = findBLECharacteristics(gatt)
            if (matchingCharacteristics.isEmpty()) {
                Log.e(TAG, "Unable to find characteristics")
                return
            }

            // log for successful discovery
            Log.d(TAG, "Services discovery is successful")

            // To enable notification
            for (bluetoothGattCharacteristic in matchingCharacteristics) {
                if (ActivityCompat.checkSelfPermission(
                        context,
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
                gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)
            }

            sharedPreferencesUtil?.deviceAddress = deviceAddress
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic written successfully")
            } else {
                Log.e(TAG, "Characteristic write unsuccessful, status: $status")
                disconnectGattServer(context)
            }
        }
    }
}