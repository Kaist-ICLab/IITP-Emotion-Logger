package kaist.iclab.wearablelogger.blu

import android.Manifest
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Build
import android.util.Log
import java.util.Timer
import java.util.TimerTask

class DataCollectionService : BLEService(), SensorEventListener {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionBluetoothScan = Manifest.permission.BLUETOOTH_SCAN
            permissionBluetoothConnect = Manifest.permission.BLUETOOTH_CONNECT
        } else {
            permissionBluetoothScan = Manifest.permission.BLUETOOTH
            permissionBluetoothConnect = Manifest.permission.BLUETOOTH
        }
        init()
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

                var fn: String = Utils.getFormattedTimeString(
                    Utils.DATE_FORMAT_TILL_DAY,
                    System.currentTimeMillis()
                ) + ".csv"
                fn = "Env_UID_" + Utils.getSmartphoneID() + "_" + fn

                // 10초마다 측정하여 csv에 저장
                if (mConnectionState == BluetoothProfile.STATE_CONNECTED || mConnectionState == BluetoothProfile.STATE_CONNECTING) {
                    val env_timestamp = System.currentTimeMillis().toString()
                    val columns = "Timestamp, Temperature, Humidity, CO2, TVOC"
                    val data: String =
                        env_timestamp + "," + temperature + "," + humidity + "," + cO2 + "," + tVOC

                    Log.v(TAG, "=====================================")
                    Log.v(TAG, "tem: " + temperature)
                    Log.v(TAG, "humidity: " + humidity)
                    Log.v(TAG, "co2: " + cO2)
                    Log.v(TAG, "tvoc: " + tVOC)

                    // TODO: Insert data here
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
            deviceAddr = sharedPreferencesUtil.getDeviceAddress()
            initialize()
            connect(deviceAddr)
        }
    }

    private fun init() {
        startForeground(2, getNotification())

        envSensorTimer = Timer()
        //        mDownloader_env = new DownloadHelper();
//        mRecorder = new RecordingHelper();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        i("onStartCommand");

        // SENSOR_DELAY_NORMAL : 20,000 msec delay

        val deviceAddress = sharedPreferencesUtil.getDeviceAddress()
        initialize()
        connect(deviceAddress)

        //        stop();

//        startService 메서드로 triggeringActivity를 시작
        //  서비스가 종료되면 서비스를 다시 실행시킴
        //  마지막 Intent를 onStartCommand의 인자로 다시 전달하지는 않음
//        return START_NOT_STICKY; // 재시작 안함
//        return START_REDELIVER_INTENT; // 강제 종료 시, 재시작하되 Intent가 전달됨. 이는 즉시 재개되어야하는 작업을 능동적으로 수행 중인 서비스에 적합. 예를 들면 파일 다운로드
        return START_STICKY // 재시작
    }

    override fun onDestroy() {
        envSensorTimer!!.cancel()
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    companion object {
        private val TAG: String = DataCollectionService::class.java.getSimpleName()
        private var envSensorTimer: Timer? = null
    }
}
