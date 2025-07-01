package kaist.iclab.wearablelogger.blu

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import androidx.annotation.Nullable
import java.util.UUID

private const val TAG = "ClientActivity"

object BluetoothHelper {
    // used to identify adding bluetooth names
    const val REQUEST_ENABLE_BT: Int = 1

    // used to request fine location permission
    const val REQUEST_FINE_LOCATION: Int = 2

    // scan period in milliseconds
    const val SCAN_PERIOD: Int = 10000

    // service and uuid
    var BLUSENSOR_SERVICE_UUID: String = "a8a82630-10a4-11e3-ab8c-f23c91aec05e"
    var UUID_SERVICE: UUID = UUID.fromString(BLUSENSOR_SERVICE_UUID)

    // sensor data
    var SENSOR_DATA: String = "a8a82631-10a4-11e3-ab8c-f23c91aec05e"
    var UUID_DATA: UUID = UUID.fromString(SENSOR_DATA)

    /**
    Find characteristics of BLE
    @param gatt gatt instance
    @return list of found gatt characteristics
    */
    fun findBLECharacteristics(gatt: BluetoothGatt): MutableList<BluetoothGattCharacteristic?> {
        val matchingCharacteristics: MutableList<BluetoothGattCharacteristic?> = mutableListOf()
        val serviceList: List<BluetoothGattService?> = gatt.getServices()
        val service: BluetoothGattService? = findGattService(serviceList)
        if (service == null) {
            return matchingCharacteristics
        }

        val characteristicList: List<BluetoothGattCharacteristic?> = service.characteristics
        for (characteristic in characteristicList) {
            if (isMatchingCharacteristic(characteristic)) {
                matchingCharacteristics.add(characteristic)
            }
        }

        return matchingCharacteristics
    }


    /**
    Find logger_data characteristic of the peripheral device
    @param gatt gatt instance
    @return found characteristic
     */
    fun findDataCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {
        //Log.d(TAG, "findDataCharacteristic SENSOR DATA " + SENSOR_DATA);
        return findCharacteristic(gatt, SENSOR_DATA)
    }

    /*
    Find the given uuid characteristic
    @param gatt gatt instance
    @param uuid_string uuid to query as string
     */
    @Nullable
    private fun findCharacteristic(
        gatt: BluetoothGatt,
        uuid: String?
    ): BluetoothGattCharacteristic? {
        val services: List<BluetoothGattService?> = gatt.getServices()

        val service: BluetoothGattService? = findGattService(services)

        if (service == null) {
            return null
        }


        val characteristicList: List<BluetoothGattCharacteristic?> = service.getCharacteristics()
        for (characteristic in characteristicList) {
            if (matchCharacteristic(characteristic, uuid)) {
                //Log.d(TAG, "uuid string  "+_uuid_string);
                return characteristic
            }
        }

        return null
    }

    /*
    Match the given characteristic and a uuid string
    @param characteristic one of found characteristic provided by the server
    @param uuid_string uuid as string to match
    @return true if matched
     */
    private fun matchCharacteristic(
        characteristic: BluetoothGattCharacteristic?,
        uuidString: String?
    ): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid: UUID = characteristic.uuid
        //Log.d(TAG, "GATT found characteristic  " + uuid.toString());
        return matchUUIDs(uuid.toString(), uuidString)
    }

    /*
    Find Gatt service that matches with the server's service
    @param service_list list of services
    @return matched service if found
     */
    @Nullable
    private fun findGattService(services: List<BluetoothGattService?>): BluetoothGattService? {
        for (service in services) {
            val serviceUuid = service?.uuid.toString()
            if (matchServiceUUIDString(serviceUuid)) {
                return service
            }
        }
        return null
    }

    /*
    Try to match the given uuid with the service uuid
    @param service_uuid_string service UUID as string
    @return true if service uuid is matched
     */
    private fun matchServiceUUIDString(serviceUuid: String): Boolean {
        return matchUUIDs(serviceUuid, BLUSENSOR_SERVICE_UUID)
    }

    /*
    Check if there is any matching characteristic
    @param characteristic query characteristic
     */
    private fun isMatchingCharacteristic(_characteristic: BluetoothGattCharacteristic?): Boolean {
        if (_characteristic == null) {
            return false
        }
        val uuid: UUID = _characteristic.uuid
        return matchCharacteristicUUID(uuid.toString())
    }

    /*
    Query the given uuid as string to the provided characteristics by the server
    @param characteristic_uuid_string query uuid as string
    @return true if the matched is found
     */
    private fun matchCharacteristicUUID(characteristicUuid: String): Boolean {
        return matchUUIDs(characteristicUuid, SENSOR_DATA)
    }

    /*
    Try to match a uuid with the given set of uuid
    @param uuid_string uuid to query
    @param matches a set of uuid
    @return true if matched
     */
    private fun matchUUIDs(uuid: String, vararg matches: String?): Boolean {
        for (match in matches) {
            if (uuid.equals(match, ignoreCase = true)) {
                return true
            }
        }

        return false
    }
}
