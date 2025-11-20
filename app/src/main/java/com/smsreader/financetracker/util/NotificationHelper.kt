package com.smsreader.financetracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smsreader.financetracker.MainActivity
import com.smsreader.financetracker.R

object NotificationHelper {
    private const val CHANNEL_ID = "transaction_alerts"
    private const val NOTIFICATION_ID_BASE = 1000
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showTransactionNotification(
        context: Context,
        amount: Double,
        merchant: String,
        category: String
    ) {
        createNotificationChannel(context)
        
        // Create intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Format amount
        val formattedAmount = "₹%.2f".format(amount)
        
        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.new_transaction))
            .setContentText("$formattedAmount at $merchant ($category)")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Show notification
        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID_BASE + System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not granted, skip notification
        }
    }
}

