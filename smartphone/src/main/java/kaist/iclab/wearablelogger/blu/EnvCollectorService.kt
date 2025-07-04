package kaist.iclab.wearablelogger.blu

import android.Manifest
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import kaist.iclab.loggerstructure.dao.EnvDao
import kaist.iclab.loggerstructure.entity.EnvEntity
import kaist.iclab.wearablelogger.util.ForegroundNotification
import kaist.iclab.wearablelogger.util.StateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Timer
import java.util.TimerTask

private const val TAG = "DataCollectionService"

class EnvCollectorService : BLEService(), SensorEventListener {
    companion object {
        private var envSensorTimer: Timer? = null
    }

    private val envDao by inject<EnvDao>()
    private val stateRepository by inject<StateRepository>()

    private var deviceAddress: String? = null
    private var sensorManager: SensorManager? = null
    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionBluetoothScan = Manifest.permission.BLUETOOTH_SCAN
            permissionBluetoothConnect = Manifest.permission.BLUETOOTH_CONNECT
        } else {
            permissionBluetoothScan = Manifest.permission.BLUETOOTH
            permissionBluetoothConnect = Manifest.permission.BLUETOOTH
        }

        envSensorTimer = Timer()
        envSensorTimerTask(envSensorTimer)
    }

    override fun onSensorChanged(event: SensorEvent?) {
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun envSensorTimerTask(mTimer: Timer?) {
        var mTimer = mTimer
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                bleConnectionCheck()

                if (mConnectionState == BluetoothProfile.STATE_CONNECTED || mConnectionState == BluetoothProfile.STATE_CONNECTING) {
                    val timestamp = System.currentTimeMillis()

                    Log.v(TAG, "=====================================")
                    Log.v(TAG, "tem: $temperature")
                    Log.v(TAG, "humidity: $humidity")
                    Log.v(TAG, "co2: $cO2")
                    Log.v(TAG, "tvoc: $tVOC")

                    CoroutineScope(Dispatchers.IO).launch {
                        envDao.insertEvent(EnvEntity(
                            timestamp = timestamp,
                            temperature = temperature,
                            humidity = humidity,
                            co2 = cO2,
                            tvoc = tVOC,
                        ))
                    }
                }
            }
        }
        mTimer = Timer()
        mTimer.schedule(timerTask, 10000, 10000)
    }

    private fun bleConnectionCheck() {
        if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
//            Context context = getApplicationContext();
//            Intent intent = new Intent(context, BluetoothActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//            stopSelf();
            Log.v(TAG, "BluetoothDisconnected")

            CoroutineScope(Dispatchers.IO).launch {
                deviceAddress = stateRepository.bluetoothAddress.first()
                initialize()
                connect(deviceAddress)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // SENSOR_DELAY_NORMAL : 20,000 msec delay
        CoroutineScope(Dispatchers.IO).launch {
            deviceAddress = stateRepository.bluetoothAddress.first()
            initialize()
            Log.v(TAG, "deviceAddress: $deviceAddress")
            connect(deviceAddress)
        }

        val notification = ForegroundNotification.getNotification(this)
        startForeground(2, notification)

        // startService 메서드로 triggeringActivity를 시작
        //  서비스가 종료되면 서비스를 다시 실행시킴
        //  마지막 Intent를 onStartCommand의 인자로 다시 전달하지는 않음
        // return START_NOT_STICKY; // 재시작 안함
        // return START_REDELIVER_INTENT; // 강제 종료 시, 재시작하되 Intent가 전달됨. 이는 즉시 재개되어야하는 작업을 능동적으로 수행 중인 서비스에 적합. 예를 들면 파일 다운로드
        return START_STICKY // 재시작
    }

    override fun onDestroy() {
        envSensorTimer!!.cancel()
        sensorManager?.unregisterListener(this)
        super.onDestroy()
    }
}
