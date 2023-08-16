package com.example.batterysaver_alert.activities

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.batterysaver_alert.R
import com.example.batterysaver_alert.broadcast_receiver.BattetyReceiver
import com.example.batterysaver_alert.broadcast_receiver.BluetoothReceiver
import com.example.batterysaver_alert.broadcast_receiver.NetworkReceiver
import com.example.batterysaver_alert.notifications.AppNotification
import pl.droidsonroids.gif.GifImageView
import java.text.DecimalFormat
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
    private lateinit var bluetooth_connectivity : TextView
    private lateinit var bluetooth_name : TextView
    private lateinit var bluetooth_Strength : TextView
    private lateinit var bluetooth_last_connect : TextView

    private lateinit var battery_gif: GifImageView

    private val batteryReceiver = BattetyReceiver()
    private val networkReceiver = NetworkReceiver()
    private val bluetoothReceiver = BluetoothReceiver()

    private val batteryDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val appNotification = AppNotification()
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
                    val TYPE_BATTERY_LOW = "low"
                    appNotification.setBatteryNotificationForUser(context,25.0f,TYPE_BATTERY_LOW,batteryLevel.toFloat())
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
            if (intent?.action == "com.example.WIFI_DATA_CHANGED") {
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
    private val bluetoothDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.BLUETOOTH_STATE_CHANGED") {
                Log.v("BluetoothReceiver","create()")
                val state = intent.getStringExtra("bluetoothState")
                // update UI
                updateBluetoothConnectivityUI(state!!)
            }
            if( intent?.action ==  "com.example.BLUETOOTH_DEVICE_CONNECTED"){
                Log.v("BluetoothReceiver","connected()")

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
        /*
        get message from pending intent
         */
        if(intent.hasExtra("message")){
            val message = intent.getStringExtra("message")
            Log.v("trigger_pendingIntent","$message")
            Toast.makeText(this, "get $message", Toast.LENGTH_SHORT).show()
        }
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

        // Register the BluetoothReceiver with the CONNECTIVITY_ACTION intent filter
        val intentFilterBluetooth = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, intentFilterBluetooth)
        // Register the bluetooth data receiver to receive custom "com.example.BLUETOOTH_STATE_CHANGED" broadcasts
        val bluetoothDataIntentFilter4 = IntentFilter("com.example.BLUETOOTH_STATE_CHANGED")
        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothDataReceiver, bluetoothDataIntentFilter4)

        // Register the BluetoothReceiver with the ACTION_ACL_CONNECTED intent filter
        val intentFilterBluetooth2 = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        registerReceiver(bluetoothReceiver, intentFilterBluetooth2)

        // Register the bluetooth data receiver to receive custom "com.example.BLUETOOTH_STATE_CHANGED" broadcasts
        val bluetoothDataIntentFilter5 = IntentFilter("com.example.BLUETOOTH_DEVICE_CONNECTED")
        LocalBroadcastManager.getInstance(this).registerReceiver(bluetoothDataReceiver, bluetoothDataIntentFilter5)


    }


    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if(bluetoothReceiver.isBluetoothOn()){
            updateBluetoothConnectivityUI("On")
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to prevent memory leaks but
        // it wont trigger it when the app is not running too
        unregisterReceiver(batteryReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(batteryDataReceiver)
        unregisterReceiver(networkReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkDataReceiver)
        unregisterReceiver(bluetoothReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bluetoothDataReceiver)
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
        bluetooth_connectivity = findViewById(R.id.bluetooth_connectivity)
        bluetooth_name = findViewById(R.id.bluetooth_name)
        bluetooth_Strength = findViewById(R.id.bluetooth_strength)
        bluetooth_last_connect = findViewById(R.id.bluetooth_last_connect)

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
        netRateTextView.text = "${roundToDecimalPlaces(status,2)} Kbps"
    }
    fun updateNetworkProviderTextView(provider : String){
        netProviderTextView.text = "$provider"
    }
    /*
    end setUI for network
     */
    /*
    start set UI for bluetooth
     */
    fun updateBluetoothConnectivityUI(status : String){
        bluetooth_connectivity.text = status
    }
    fun updateBluetoothNameUI(name : String){
        bluetooth_name.text = name
    }
    fun updateBluetoothStrengthUI(status : String){
        bluetooth_Strength.text = status
    }
    fun updateBluetoothLastConnectUI(name : String){
        bluetooth_last_connect.text = name
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
    fun roundToDecimalPlaces(number: Double, decimalPlaces: Int): String {
        val pattern = when (decimalPlaces) {
            0 -> "#"
            1 -> "#.#"
            2 -> "#.##"
            // Add more cases if needed
            else -> "#.###" // Default pattern for more than 2 decimal places
        }
        val decimalFormat = DecimalFormat(pattern)
        return decimalFormat.format(number)
    }
}