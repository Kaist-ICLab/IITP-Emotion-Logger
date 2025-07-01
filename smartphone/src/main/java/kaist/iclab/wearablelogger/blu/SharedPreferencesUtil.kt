package kaist.iclab.wearablelogger.blu

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesUtil {
    private val sharedPreferences: SharedPreferences?
        get() = Companion.sharedPreferences


    var deviceAddress: String?
        get() {
            val address = this.sharedPreferences!!.getString(
                DEVICE_ADDRESS,
                null
            )
            return address
        }
        /******************
         * BLE Setting
         */
        set(address) {
            this.sharedPreferences!!.edit(commit = true) {
                putString(DEVICE_ADDRESS, address)
            }
        }

    var workStatus: Boolean
        get() {
            val status = this.sharedPreferences!!.getBoolean(
                WORK_STATUS,
                false
            )
            return status
        }
        set(status) {
            this.sharedPreferences!!.edit(commit = true) {
                putBoolean(WORK_STATUS, status)
            }
        }

    var submitCount: Int
        get() {
            val cnt =
                this.sharedPreferences!!.getInt(SUBMIT_COUNT, 0)
            return cnt
        }
        set(cnt) {
            this.sharedPreferences!!.edit(commit = true) {
                putInt(SUBMIT_COUNT, cnt)
            }
        }

    companion object {
        private const val SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES_NAME"
        private const val WORK_STATUS = "WORK_STATUS"
        private const val SUBMIT_COUNT = "SUBMIT_COUNT"
        private const val DEVICE_ADDRESS = "DEVICE_ADDRESS"


        private var sharedPreferences: SharedPreferences? = null

        fun getInstance(context: Context): SharedPreferencesUtil {
            if (sharedPreferences == null) sharedPreferences = context.getSharedPreferences(
                SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
            )
            return SharedPreferencesUtil()
        }
    }
}