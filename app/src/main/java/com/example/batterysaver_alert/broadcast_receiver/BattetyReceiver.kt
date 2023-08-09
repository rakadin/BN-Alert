package com.example.batterysaver_alert.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.text.format.DateUtils
import android.widget.TextView
import com.example.batterysaver_alert.R
import com.example.batterysaver_alert.activities.HomeActivity
import pl.droidsonroids.gif.GifImageView

class BattetyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
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

            // get text ids
            val percentTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.percent_in)
            val capacityText = (context as? HomeActivity)?.findViewById<TextView>(R.id.capacity_in)
            val temperatureTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.temperature_in)
            val chargeStateTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.chargeStatus_in)
            val lastCharge = (context as? HomeActivity)?.findViewById<TextView>(R.id.chargeLast_in)
            val battery_gif= (context as? HomeActivity)?.findViewById<GifImageView>(R.id.bat_gif)


            // handle battery information
            temperatureTextView?.text = "$temperature °C" // set temperature
            percentTextView?.text = "$batteryLevel%"// set percent now
            capacityText?.text = "$scale mah" // set battery capacity
            if(isCharging){
                chargeStateTextView?.text = "Charging"
                lastCharge?.text = "Now"
                battery_gif?.setImageResource(R.drawable.battery_charge)
            }
            if(isFull){
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

        }

        // when the charger is unplug- static way ( even when the app is not running )
        if (intent?.action == Intent.ACTION_POWER_DISCONNECTED) {
            val unplugTimeMillis = System.currentTimeMillis()

            // Save the unplug time to shared preferences
            val sharedPreferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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

//        // when the charger is plug in
//        if(intent?.action == Intent.ACTION_POWER_CONNECTED){
//            val chargeLastTextView = (context as? HomeActivity)?.findViewById<TextView>(R.id.chargeLast_in)
//            chargeLastTextView?.text = "Now"
//        }
    }
}