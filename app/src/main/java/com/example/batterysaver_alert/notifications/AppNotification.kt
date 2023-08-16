package com.example.batterysaver_alert.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.batterysaver_alert.R
import com.example.batterysaver_alert.activities.HomeActivity

class AppNotification {
    private val TYPE_BATTERY_LOW = "low"
    private val TYPE_BATTERY_HOT = "temperature"
    private val TYPE_BATTERY_LOW_ALARM = "alarm_low"
    private val TYPE_BATTERY_HOT_ALARM = "temperature"
    private val TYPE_BATTERY_HOT_WHEN_CHARGING = "too_hot_to_charge"
    // create notification for user
    fun setBatteryNotificationForUser(context: Context?, limit: Float?, type:String, temp: Float){
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        /*
        create pending intent
         */
        val targetActivity = Intent(context, HomeActivity::class.java)
        targetActivity.putExtra("message","welcome back from pending intent")
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            targetActivity,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create a notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "battery_channel",
                "Battery Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(context, "battery_channel")
        // Create the battery low notification
        if( type == TYPE_BATTERY_LOW){
            notificationBuilder.setContentTitle("Battery Alert")
                .setContentTitle("Plug charger in please😥")
                .setContentText("Battery level is below $limit% 🥺")
                .setContentIntent(pendingIntent)// testing pending intent
                .setSmallIcon(R.drawable.battery_warn)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        }
        // Create the battery is too hot notification
        if( type == TYPE_BATTERY_HOT){
            notificationBuilder.setContentTitle("Battery Alert")
                .setContentText("Your battery temperature is too hot🥵! $temp°C")
                .setSmallIcon(R.drawable.battery_warn)
                .setContentTitle("TOO HOT😱")
                .setContentIntent(pendingIntent)// testing pending intent
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        }
        if(type ==TYPE_BATTERY_LOW_ALARM){
            notificationBuilder.setContentTitle("Battery Alert")
                .setContentText("Charge your phone now! $limit% left")
                .setContentTitle("BATTERY LOW😱")
                .setSmallIcon(R.drawable.battery_warn)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Set the notification to be automatically canceled
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set the notification sound

        }
        if(type ==TYPE_BATTERY_HOT_ALARM){
            notificationBuilder.setContentTitle("Battery Alert")
                .setContentTitle("TOO HOT😱")
                .setContentText("Stop using your phone! $temp°C")
                .setSmallIcon(R.drawable.battery_warn)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Set the notification to be automatically canceled
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set the notification sound
        }
        if(type ==TYPE_BATTERY_HOT_WHEN_CHARGING){
            notificationBuilder.setContentTitle("Battery Alert")
                .setContentTitle("TOO HOT TO CHARGE😱")
                .setContentText("Your phone is too hot to be charged! $temp°C")
                .setSmallIcon(R.drawable.battery_warn)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Set the notification to be automatically canceled
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set the notification sound
        }
        // Send the notification
        notificationManager.notify(1, notificationBuilder.build())
    }
}