package kaist.iclab.wearablelogger.blu

import android.Manifest
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import kaist.iclab.wearablelogger.blu.BluetoothHelper.UUID_DATA
import java.util.UUID

open class BLEService : Service() {
    companion object {
        private val TAG: String = BLEService::class.java.getSimpleName()
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 2

        var temperature: Double = 0.0
            private set
        var humidity: Double = 0.0
            private set
        var cO2: Int = 0
            private set
        var tVOC: Int = 0
            private set
    }

    private val mBinder: IBinder = LocalBinder()
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    var mConnectionState: Int = STATE_DISCONNECTED

    var permissionBluetoothScan: String? = null
    var permissionBluetoothConnect: String? = null

    // Implements callback methods for GATT events that the app cares about.
    // For example, connection change and services discovered.
    private val mGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (ActivityCompat.checkSelfPermission(
                    this@BLEService,
                    permissionBluetoothConnect
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.i(TAG, "no permission to bluetooth_connect.")
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(
                    TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt?.discoverServices()
                )
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }

            // find discovered characteristics
            val matchingCharacteristics: MutableList<BluetoothGattCharacteristic?> =
                BluetoothHelper.findBLECharacteristics(gatt)
            if (matchingCharacteristics.isEmpty()) {
                Log.e(TAG, "Unable to find characteristics")
                return
            }
            // log for successful discovery
            Log.d(TAG, "Services discovery is successful")

            // To enable notification
            for (bluetoothGattCharacteristic in matchingCharacteristics) {
                if (ActivityCompat.checkSelfPermission(
                        this@BLEService,
                        permissionBluetoothConnect
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
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)

            if(status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Characteristic read unsuccessful, status: $status")
                // Trying to read from the Time Characteristic? It doesn't have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();

                return
            }

            Log.d(TAG, "Characteristic read successfully")
            broadcastUpdate(characteristic)

            if (UUID_DATA == characteristic.uuid) {
                val sensorData = value

                //TEMPERATURE (signed, factor 100)
                val signedTemperature =
                    ((sensorData[3].toInt() and 0xff) or ((sensorData[4].toInt() shl 8) and 0xff00)).toShort()
                temperature = signedTemperature / 100.0

                //HUMIDITY (unsigned, factor 100)
                humidity =
                    ((sensorData[5].toInt() and 0xff) or ((sensorData[6].toInt() shl 8) and 0xff00)) / 100.0

                //CO2 (unsigned, factor 1)
                cO2 = (sensorData[7].toInt() and 0xff) or ((sensorData[8].toInt() shl 8) and 0xff00)

                //TVOC (unsigned, factor 1)
                tVOC = (sensorData[9].toInt() and 0xff) or ((sensorData[10].toInt() shl 8) and 0xff00)
            } else {
                val data = value
                if (data.isNotEmpty()) {
                    val stringBuilder = StringBuilder(data.size)
                    for (byteChar in data) stringBuilder.append(String.format("%02X ", byteChar))
                }
            }

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            Log.d(TAG, "Characteristic read successfully")
            broadcastUpdate(characteristic)
        }
    }

    private fun broadcastUpdate(characteristic: BluetoothGattCharacteristic) {
//        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml

        if (ActivityCompat.checkSelfPermission(
                this,
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

        if (UUID_DATA == characteristic.uuid) {
            val flag: Int = characteristic.properties
            val format = -1


            mBluetoothGatt?.readCharacteristic(characteristic)
        } else {
            // For all other profiles, writes the data formatted in HEX.
            mBluetoothGatt?.readCharacteristic(characteristic)
        }
    }

    inner class LocalBinder : Binder() {
        val service: BLEService
            get() = this@BLEService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")

            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
            && mBluetoothGatt != null
        ) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permissionBluetoothConnect
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "Bluetooth permission issues.")
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false
            }
            if (mBluetoothGatt!!.connect()) {
                Log.w(TAG, "mBluetoothGatt.connect: true")
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                Log.w(TAG, "mBluetoothGatt.connect: false")
                return false
            }
        }

        Log.v(TAG, "address: $address")
        val device: BluetoothDevice? = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                permissionBluetoothConnect
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
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        if (permissionBluetoothConnect == null || ActivityCompat.checkSelfPermission(
                this,
                permissionBluetoothConnect
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
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                permissionBluetoothConnect
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
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                permissionBluetoothConnect
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
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Sensor Data Measurement.
        if (UUID_DATA == characteristic.uuid) {
            val descriptor: BluetoothGattDescriptor = characteristic.getDescriptor(
                UUID.fromString(BluetoothHelper.BLUSENSOR_SERVICE_UUID)
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mBluetoothGatt!!.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            }
        }
    }


//    val supportedGattServices: MutableList<BluetoothGattService>?
//        /**
//         * Retrieves a list of supported GATT services on the connected device. This should be
//         * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
//         *
//         * @return A `List` of supported services.
//         */
//        get() {
//            if (mBluetoothGatt == null) return null
//
//            return mBluetoothGatt!!.getServices()
//        }
}