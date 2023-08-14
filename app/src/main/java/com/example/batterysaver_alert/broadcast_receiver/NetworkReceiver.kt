package com.example.batterysaver_alert.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.text.DecimalFormat
import java.util.*

class NetworkReceiver : BroadcastReceiver() {
    private var timer: Timer? = null //An instance of the Timer class that is used to schedule repeated tasks at fixed intervals.
    private var uploadBytesStart: Long = 0 // This variable holds the total number of bytes uploaded (sent) over the network at the start of tracking
    private var handler: Handler = Handler(Looper.getMainLooper())//This is an instance of the Handler class, which is used to post tasks to the main thread's message queue. It's used to log the calculated rates on the main thread.
    private var downloadBytesStart: Long = 0// Similar to uploadBytesStart, this variable holds the total number of bytes downloaded (received) over the network at the start of tracking.
    private var networkSpeedMbps: Double = 0.0

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val connectivityManager =
                context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                val networkType = activeNetworkInfo.type
                val networkTypeName = activeNetworkInfo.typeName
                // You can now use this information as needed
                Log.d("NetworkReceiver", "Network Type: $networkType")
                Log.d("NetworkReceiver", "Network Type: $networkTypeName")


                //send data to activity
                val localIntent = Intent("com.example.WIFI_DATA_CHANGED")
                // Start a timer to calculate rates
                startRateCalculationTimer(localIntent,context)
                localIntent.putExtra("networkType", networkTypeName)
                localIntent.putExtra("provider", getNetworkProviderName(context))
                Log.d("NetworkReceiver", "Network provider: ${getNetworkProviderName(context)}")
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
            }
        }
    }

    private fun getNetworkStrength(context: Context): Int {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val signalStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telephonyManager.signalStrength
        } else {
            TODO("VERSION.SDK_INT < P")
        }

        // The signalStrength value is in dBm
        // You can convert it to a more meaningful representation or use it as needed
        return signalStrength!!.level
    }
    private fun startRateCalculationTimer(localIntent: Intent, context: Context) {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                calculateAndLogRates()
                val networkStrength = getNetworkStrength(context)
                val uploadRate = TrafficStats.getTotalTxBytes()/ 1024 // Upload rate in KB
                val downloadRate = TrafficStats.getTotalRxBytes()/ 1024 // Download rate in KB
                localIntent.putExtra("networkStrength", returnConnectStrength(networkStrength))
                localIntent.putExtra("networkSpeed",networkSpeedMbps)
            }
        }, 0, 1000) // Calculate rates every 1 second (adjust interval as needed)
    }
    private fun calculateAndLogRates() {
        val uploadBytesEnd = TrafficStats.getTotalTxBytes()
        val downloadBytesEnd = TrafficStats.getTotalRxBytes()

        val timeIntervalInSeconds = 1.0 // 1 second interval
        val uploadRateMbps = ((uploadBytesEnd - uploadBytesStart) * 8.0 / 1024 / 1024 / timeIntervalInSeconds)
        val downloadRateMbps = ((downloadBytesEnd - downloadBytesStart) * 8.0 / 1024 / 1024 / timeIntervalInSeconds)

        // Calculate network speed in Mbps
        networkSpeedMbps = (uploadRateMbps + downloadRateMbps) / 2.0
        // Log the calculated rates and speed
        Log.d("NetworkReceiver", "Upload Rate: ${roundToDecimalPlaces(uploadRateMbps,1)} Mbps")
        Log.d("NetworkReceiver", "Download Rate: ${roundToDecimalPlaces(downloadRateMbps,1)} Mbps")
        Log.d("NetworkReceiver", "Network Speed: ${roundToDecimalPlaces(networkSpeedMbps,1)} Mbps")

        // Update the start values for the next interval
        uploadBytesStart = uploadBytesEnd
        downloadBytesStart = downloadBytesEnd
        // Log the calculated rates and speed
        Log.d("NetworkReceiver", "Upload Rate: ${roundToDecimalPlaces(uploadRateMbps,1)} Mbps")
        Log.d("NetworkReceiver", "Download Rate: ${roundToDecimalPlaces(downloadRateMbps,1)} Mbps")
        Log.d("NetworkReceiver", "Network Speed: ${roundToDecimalPlaces(networkSpeedMbps,1)} Mbps")

    }
    fun getNetworkProviderName(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.networkOperatorName
    }
    private fun returnConnectStrength(strength : Int) : String{
//        0-2: Weak signal
//        3-4: Moderate signal
//        5-7: Good signal
//        8-31: Excellent signal
        var strengResult = ""
        when(strength){
            in 0..2 -> strengResult="Weak"
            in 3..4 -> strengResult="Moderate"
            in 5..7 -> strengResult="Good"
            in 8..31 -> strengResult="Excellent"
            else -> strengResult="Unknown"
        }
        return strengResult
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
