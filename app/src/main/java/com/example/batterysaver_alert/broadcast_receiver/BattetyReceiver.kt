package com.example.batterysaver_alert.broadcast_receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.text.Html
import android.text.format.DateUtils
import android.util.Log
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.batterysaver_alert.R
import com.example.batterysaver_alert.activities.HomeActivity
import pl.droidsonroids.gif.GifImageView

class BattetyReceiver : BroadcastReceiver() {
    private val TYPE_BATTERY_LOW = "low"
    private val TYPE_BATTERY_HOT = "temperature"
    private val TYPE_BATTERY_LOW_ALARM = "alarm_low"
    private val TYPE_BATTERY_HOT_ALARM = "temperature"
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.( Dynamic way)
        if(intent?.action == Intent.ACTION_BATTERY_CHANGED)
        {

            val chargingStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = chargingStatus == BatteryManager.BATTERY_STATUS_CHARGING
            val isFull = chargingStatus == BatteryManager.BATTERY_STATUS_FULL
           // val isFull2 = chargingStatus == BatteryManager.BA

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) // current mah
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)// total mah
            val batteryLevel = (level.toFloat() / scale.toFloat()) * 100.0f
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f

            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)            // get text ids to set text

            val percentTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.percent_in)
            val capacityText = (context as? HomeActivity)?.findViewById<TextView>(R.id.capacity_in)
            val temperatureTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.temperature_in)
            val chargeStateTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.chargeStatus_in)
            val lastCharge = (context as? HomeActivity)?.findViewById<TextView>(R.id.chargeLast_in)
            val battery_gif= (context as? HomeActivity)?.findViewById<GifImageView>(R.id.bat_gif)


            // handle battery information
            temperatureTextView?.text = "$temperature °C" // set temperature
            percentTextView?.text = "$batteryLevel%"// set percent now
            capacityText?.text = "$capacity mah" // set battery capacity
            if(isCharging){ // if the phone is charging
                chargeStateTextView?.text = "Charging"
                lastCharge?.text = "Now"
                battery_gif?.setImageResource(R.drawable.battery_charge)
            }
            if(isFull){ // if the battery is fully charge
                chargeStateTextView?.text = "Full"
                battery_gif?.setImageResource(R.drawable.battery_full)
            }
            if(isCharging == false){
                chargeStateTextView?.text = "Not charge"
                //handle the battery level if not charge gif
                if(batteryLevel >30 && batteryLevel <80){
                    battery_gif?.setImageResource(R.drawable.battery_half)
                }
                if(batteryLevel >=80){
                    battery_gif?.setImageResource(R.drawable.battery_full)
                }
                if (batteryLevel <= 30){
                    battery_gif?.setImageResource(R.drawable.battery_low)
                }
            }

            // check if user setting is enable some of this notification below
            val isNotiTextChecked = sharedPreferences?.getBoolean("noti_text",false)
            val isNotiSoundChecked = sharedPreferences?.getBoolean("noti_sound",false)
            val low_limitString = sharedPreferences?.getString("percent_limit","10%")
            val heat_limitString = sharedPreferences?.getString("heat_limit","40°C")

            //get 10 from 10% string
            val numeric_percent_Value = low_limitString?.replace("%", "")?.toFloatOrNull()
            val numeric_heat_Value = heat_limitString?.replace("°C", "")?.toFloatOrNull()

            Log.v("check_sound",isNotiSoundChecked.toString())
            Log.v("check_text",isNotiTextChecked.toString())
            Log.v("get_int",numeric_percent_Value.toString())
            Log.v("get_int",numeric_percent_Value!!.toString())


            if(isNotiTextChecked == true){ // if noti on -> check next
                if(isNotiSoundChecked == false){ // else noti with no sound
                    // set limit to send notification to user
                    Log.v("check_text","text_called")
                    if(batteryLevel <= numeric_percent_Value!!){
                        // should save database in this
                        setBatteryNotificationForUser(context,10,TYPE_BATTERY_LOW,temperature)
                    }
                    if(temperature >= numeric_heat_Value!!){
                        setBatteryNotificationForUser(context,10,TYPE_BATTERY_HOT,temperature)
                    }
                }
                else{ // if noti sound checked -> noti with sound
                    // set limit to send notification with sound to user
                    Log.v("check_text","sound_called")
                    if(batteryLevel <= numeric_percent_Value!!){
                        // should save database in this
                        setBatteryNotificationForUser(context,10,TYPE_BATTERY_LOW_ALARM,temperature)
                    }
                    if(temperature>= numeric_heat_Value!!){
                        setBatteryNotificationForUser(context,10,TYPE_BATTERY_HOT_ALARM,temperature)
                    }
                }

            }

//

        }

        // when the charger is unplug- static way ( even when the app is not running )
        if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
            val unplugTimeMillis = System.currentTimeMillis()

            // Save the unplug time to shared preferences
            sharedPreferences?.edit()?.putLong("unplug_time", unplugTimeMillis)?.apply()

            val chargeLastTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.chargeLast_in)

            if (unplugTimeMillis != 0L) {
                val currentTimeMillis = System.currentTimeMillis()
                //val timeDifferenceMillis = currentTimeMillis - unplugTimeMillis
                // get last time plug in
                val timeAgo = DateUtils.getRelativeTimeSpanString(unplugTimeMillis, currentTimeMillis, DateUtils.MINUTE_IN_MILLIS)
                // get textview to set
                chargeLastTextView?.text = "$timeAgo"

            } else {
                // The phone has not been unplugged since the app was installed or preferences were cleared
            }
        }
    }
    // create notification for user
    private fun setBatteryNotificationForUser(context: Context?,limit : Int, type :String, temperature: Float){
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
                .setSmallIcon(R.drawable.battery_warn)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        }
        // Create the battery is too hot notification
        if( type == TYPE_BATTERY_HOT){
            notificationBuilder.setContentTitle("Battery Alert")
                .setContentText("Your battery temperature is too hot🥵! $temperature°C")
                .setSmallIcon(R.drawable.battery_warn)
                .setContentTitle("TOO HOT😱")
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
                .setContentText("Stop using your phone! $temperature°C")
                .setSmallIcon(R.drawable.battery_warn)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Set the notification to be automatically canceled
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set the notification sound
        }
        // Send the notification
        notificationManager.notify(1, notificationBuilder.build())
    }
}
