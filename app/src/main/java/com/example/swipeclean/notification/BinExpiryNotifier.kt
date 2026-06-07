package com.example.swipeclean.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.swipeclean.MainActivity
import com.example.swipeclean.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinExpiryNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        ensureChannel()
    }

    fun notifyExpiringSoon(itemCount: Int, daysRemaining: Int) {
        if (itemCount <= 0) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val itemsLabel = if (itemCount == 1) {
            context.getString(R.string.notification_bin_expiry_items_one)
        } else {
            context.getString(R.string.notification_bin_expiry_items_other)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_BIN,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(context.getString(R.string.notification_bin_expiry_title))
            .setContentText(
                context.getString(
                    R.string.notification_bin_expiry_body,
                    itemCount,
                    itemsLabel,
                    daysRemaining
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_bin_expiry),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_bin_expiry_desc)
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "bin_expiry_reminders"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN_BIN = 4201
    }
}
