package com.example.batterysaver_alert.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.batterysaver_alert.R
import com.example.batterysaver_alert.broadcast_receiver.BattetyReceiver
import com.example.batterysaver_alert.broadcast_receiver.NetworkReceiver
import pl.droidsonroids.gif.GifImageView
import java.util.logging.Level

class HomeActivity : AppCompatActivity() {
    private lateinit var percentTextView : TextView
    private lateinit var capacityText : TextView
    private lateinit var temperatureTextView :TextView
    private lateinit var chargeStateTextView: TextView
    private lateinit var netTypeTextView : TextView
    private lateinit var netStatusText : TextView
    private lateinit var netRateTextView :TextView
    private lateinit var netProviderTextView: TextView
    private lateinit var lastCharge : TextView
    private lateinit var battery_gif: GifImageView

    private val batteryReceiver = BattetyReceiver()
    private val networkReceiver = NetworkReceiver()

    private val batteryDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.v("network_connect","connected")
            if (intent?.action == "com.example.BATTERY_DATA_CHANGED") {
                Log.v("network_connect","connected")
                val batteryLevel = intent.getIntExtra("batteryLevel", -1)/1.0f
                val batteryTemperature = intent.getFloatExtra("batteryTemperature", -1.0f)
                val batteryCapacity = intent.getIntExtra("batteryCapacity",1000)
                val isCharging = intent.getBooleanExtra("isCharging", false)
                val isFull = intent.getBooleanExtra("isFull", false)

                updatePercentage(batteryLevel)
                updateTemperature(batteryTemperature)
                updateCapacity(batteryCapacity)
                if(isCharging){// if is charging
                    updateBatteryGifWhenCharging(batteryTemperature)
                }
                else{
                    updateBatteryGifWhenNotCharging(batteryTemperature,batteryLevel)
                }

                if(isFull){
                    updateStateIfFull()
                }
            }
            if(intent?.action == "com.example.BATTERY_POWER_DISCONNECTED"){

            }
        }
    }
    private val networkDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                val networkType = intent.getStringExtra("networkType")
                val networkStrength = intent.getStringExtra("networkStrength")
                val networkSpeed = intent.getDoubleExtra("networkSpeed", 0.0)
                val networkProvider = intent.getStringExtra("provider")

                // update UI
                updateNetworkTypeTextView(networkType!!)
                updateNetworkStatusTextView(networkStrength!!)
                updateNetworkRateTextView(networkSpeed)
                updateNetworkProviderTextView(networkProvider!!)

            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("trigger","create()")
        super.onCreate(savedInstanceState)
        // Hide the status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()// hide the action bar
        //get ids//setlast charge
        val lastchargeText : TextView = findViewById(R.id.chargeLast_in)
        setLastCharge(lastchargeText)// set last time user plug in charger

        getIDs()// get ids

        // Register the battery receiver to receive ACTION_BATTERY_CHANGED broadcasts
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, batteryIntentFilter)

        // Register the battery data receiver to receive custom "com.example.BATTERY_DATA_CHANGED" broadcasts
        val batteryDataIntentFilter = IntentFilter("com.example.BATTERY_DATA_CHANGED")
        LocalBroadcastManager.getInstance(this).registerReceiver(batteryDataReceiver, batteryDataIntentFilter)

        // Register the battery receiver to receive ACTION_BATTERY_CHANGED broadcasts
        val batteryIntentFilter2 = IntentFilter(Intent.ACTION_POWER_DISCONNECTED)
        registerReceiver(batteryReceiver, batteryIntentFilter2)

        // Register the battery data receiver to receive custom "com.example.BATTERY_DATA_CHANGED" broadcasts
        val batteryDataIntentFilter2 = IntentFilter("com.example.BATTERY_POWER_DISCONNECTED")
        LocalBroadcastManager.getInstance(this).registerReceiver(batteryDataReceiver, batteryDataIntentFilter2)

        // Register the NetworkReceiver with the CONNECTIVITY_ACTION intent filter
        val intentFilterNetwork = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilterNetwork)
        // Register the network data receiver to receive custom "com.example.WIFI_DATA_CHANGED" broadcasts
        val networkDataIntentFilter3 = IntentFilter("com.example.WIFI_DATA_CHANGED")
        LocalBroadcastManager.getInstance(this).registerReceiver(networkDataReceiver, networkDataIntentFilter3)


    }

    override fun onResume() {
        super.onResume()

    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to prevent memory leaks but
        // it wont trigger it when the app is not running too
        unregisterReceiver(batteryReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(batteryDataReceiver)
        unregisterReceiver(networkReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDataReceiver)
    }
    fun getIDs(){
        percentTextView = findViewById(R.id.percent_in)
        capacityText = findViewById(R.id.capacity_in)
        temperatureTextView = findViewById(R.id.temperature_in)
        chargeStateTextView = findViewById(R.id.chargeStatus_in)
        lastCharge = findViewById(R.id.chargeLast_in)
        battery_gif= findViewById(R.id.bat_gif)
        netTypeTextView = findViewById(R.id.netType_in)
        netStatusText = findViewById(R.id.netStatus_in)
        netRateTextView = findViewById(R.id.netRate_in)
        netProviderTextView = findViewById(R.id.provider_in)
    }
    /*
    update UI for Battery
     */
    fun updatePercentage(percent:Float){
        percentTextView.text = "$percent%"// set percent now

    }

    fun updateTemperature(temperature : Float){
        temperatureTextView.text = "$temperature °C" // set temperature
    }

    fun updateCapacity(capacity : Int){
        capacityText?.text = "$capacity mah" // set battery capacity
    }

    fun updateBatteryGifWhenCharging(temperature : Float){
        chargeStateTextView?.text = "Charging"
        lastCharge?.text = "Now"
        battery_gif?.setImageResource(R.drawable.battery_charge)
        // when the phone in charging but temperature is too HOT >55 celcius
        if(temperature >= 55){
            Log.d("trigger","trigger background when not running")
           // @/
        }
    }
    fun updateBatteryGifWhenNotCharging(temperature : Float,percent: Float){
        chargeStateTextView?.text = "Not charge"
        setLastCharge(lastCharge)
        updateStateIfNotCharge(percent)
        // when the phone in charging but temperature is too HOT >55 celcius

        if(temperature >= 55){
            Log.d("trigger","trigger background when not running")
            // @/
        }
    }
    fun updateStateIfFull(){
        chargeStateTextView?.text = "Full"
        battery_gif?.setImageResource(R.drawable.battery_full)
    }

    fun updateStateIfNotCharge(batteryLevel : Float){
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
    /*
    end update UI for Battery
     */

    /*
    set UI for networkReceiver
     */
    fun updateNetworkTypeTextView(text : String){
        netTypeTextView.text = "$text"
    }
    fun updateNetworkStatusTextView(text : String){
        netStatusText.text = "$text"
    }
    fun updateNetworkRateTextView(status : Double){
        netTypeTextView.text = "$status Mbps"
    }
    fun updateNetworkProviderTextView(provider : String){
        netProviderTextView.text = "$provider"
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