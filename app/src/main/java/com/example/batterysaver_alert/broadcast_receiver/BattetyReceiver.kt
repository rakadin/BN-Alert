package com.example.batterysaver_alert.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.text.format.DateUtils
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.batterysaver_alert.notifications.AppNotification


class BattetyReceiver : BroadcastReceiver() {
    private val TYPE_BATTERY_LOW = "low"
    private val TYPE_BATTERY_HOT = "temperature"
    private val TYPE_BATTERY_LOW_ALARM = "alarm_low"
    private val TYPE_BATTERY_HOT_ALARM = "temperature"
    private val TYPE_BATTERY_HOT_WHEN_CHARGING = "too_hot_to_charge"

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val appNotification = AppNotification()
        // check if user setting is enable some of this notification below
        val isNotiTextChecked = sharedPreferences?.getBoolean("noti_text",false)
        val isNotiSoundChecked = sharedPreferences?.getBoolean("noti_sound",false)
        val low_limitString = sharedPreferences?.getString("percent_limit","10%")
        val heat_limitString = sharedPreferences?.getString("heat_limit","40°C")
        //get 10 from 10% string
        val numeric_percent_Value = low_limitString?.replace("%", "")?.toFloatOrNull()
        val numeric_heat_Value = heat_limitString?.replace("°C", "")?.toFloatOrNull()

        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f
            val capacity = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1000) // Use EXTRA_SCALE for capacity
            val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
            val isFull = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_FULL

            //send data to activity
            val localIntent = Intent("com.example.BATTERY_DATA_CHANGED")
            localIntent.putExtra("batteryLevel", batteryLevel)
            localIntent.putExtra("batteryTemperature", temperature)
            localIntent.putExtra("batteryCapacity",capacity)
            localIntent.putExtra("isCharging", isCharging)
            localIntent.putExtra("isFull", isFull)
            LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

            /*
            check if condition apply, send notification
             */
            if(isNotiTextChecked == true){ // if noti on -> check next
                if(isNotiSoundChecked == false){ // else noti with no sound
                    // set limit to send notification to user
                    Log.v("check_text","text_called")
                    if(batteryLevel <= numeric_percent_Value!!){// battery low
                        // should save database in this
                        appNotification.setBatteryNotificationForUser(context,numeric_percent_Value,TYPE_BATTERY_LOW,batteryLevel.toFloat())
                    }
                    if(temperature >= numeric_heat_Value!!){// batterh heat hight
                        appNotification.setBatteryNotificationForUser(context,numeric_heat_Value,TYPE_BATTERY_HOT,temperature)
                    }
                }
                else{ // if noti sound checked -> noti with sound
                    // set limit to send notification with sound to user
                    Log.v("check_text","sound_called")
                    if(batteryLevel <= numeric_percent_Value!!){
                        // should save database in this
                        appNotification.setBatteryNotificationForUser(context,numeric_percent_Value,TYPE_BATTERY_LOW_ALARM,temperature)
                    }
                    if(temperature>= numeric_heat_Value!!){
                        appNotification.setBatteryNotificationForUser(context,numeric_percent_Value,TYPE_BATTERY_HOT_ALARM,temperature)
                    }
                }

            }
            if(isCharging){ // if charging and the phone is too hot -> send notification
                if(temperature>=55){
                    appNotification.setBatteryNotificationForUser(context,55.0f,TYPE_BATTERY_HOT_WHEN_CHARGING,temperature)
                }
            }
//

        }
         //when the charger is unplug
        if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
            Log.e("trigger_background","screen on")
            val unplugTimeMillis = System.currentTimeMillis()
            // Save the unplug time to shared preferences
            sharedPreferences?.edit()?.putLong("unplug_time", unplugTimeMillis)?.apply()
            if (unplugTimeMillis != 0L) {
                val currentTimeMillis = System.currentTimeMillis()
                //val timeDifferenceMillis = currentTimeMillis - unplugTimeMillis
                // get last time plug in
                val timeAgo = DateUtils.getRelativeTimeSpanString(unplugTimeMillis, currentTimeMillis, DateUtils.MINUTE_IN_MILLIS)
                //send data to activity
                val localIntent = Intent("com.example.BATTERY_POWER_DISCONNECTED")
                localIntent.putExtra("chargeDisconnect", timeAgo)
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
            } else {
                // The phone has not been unplugged since the app was installed or preferences were cleared
            }
        }
        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            // Perform actions when the user unlocks the device
            Log.d("trigger_action","present")
        }
    }
}
