package com.example.batterysaver_alert.activities

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.batterysaver_alert.R
import com.example.batterysaver_alert.broadcast_receiver.BattetyReceiver

class HomeActivity : AppCompatActivity() {
    private val batteryInfoReceiver = BattetyReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()// hide the action bar
        //setlast charge

        //get ids
        val lastchargeText : TextView = findViewById(R.id.chargeLast_in)
        setLastCharge(lastchargeText)

        // Register the receiver
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intentFilter2 = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        val intentFilter3 = IntentFilter(Intent.ACTION_POWER_CONNECTED)

        // register broadcast receiver
        registerReceiver(batteryInfoReceiver, intentFilter)
        registerReceiver(batteryInfoReceiver, intentFilter2)
        registerReceiver(batteryInfoReceiver, intentFilter3)


    }
    override fun onDestroy() {
        super.onDestroy()

        // Unregister the receiver to prevent memory leaks
        unregisterReceiver(batteryInfoReceiver)
    }
    // this fun is use to get unplug time from shared preference and calculate timeAgo to set text
    fun setLastCharge(textview: TextView){
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val unplugTimeMillis = sharedPreferences.getLong("unplug_time", 0)
        val currentTimeMillis = System.currentTimeMillis()
        val timeAgo = DateUtils.getRelativeTimeSpanString(unplugTimeMillis, currentTimeMillis, DateUtils.MINUTE_IN_MILLIS)
        textview?.text = "$timeAgo"
    }
    // button open history activity func
    fun openHistoryFromHome(view: View) {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
    // button open setting activity func
    fun openSettingFromHome(view: View) {
        val intent = Intent(this,SettingActivity::class.java)
        startActivity(intent)
    }
}