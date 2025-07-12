package kaist.iclab.wearablelogger.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService

object ForegroundNotification {
    private var notification: Notification? = null

    private const val CHANNEL_ID = "ABCLogger"
    private const val CHANNEL_NAME = "ABCLogger_mobile"
    private const val CHANNEL_TEXT = "ABCLogger is collecting your data"

    private fun generateNotification(context: Context) {
        // Create notification channel first
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(context, NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(CHANNEL_NAME)
                .setContentText(CHANNEL_TEXT)
                .build()
    }

    fun getNotification(context: Context): Notification {
        while(notification == null) generateNotification(context)
        return notification!!
    }
}