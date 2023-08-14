package com.example.batterysaver_alert.broadcast_receiver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class BluetoothReceiver : BroadcastReceiver() {
    private var connectedDevice: BluetoothDevice? = null

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        // The intent parameter contains information about the broadcast event.
        // It may be null if there's no data being broadcasted or the broadcast was not properly formatted.

        when (intent?.action) {
            // Check if the broadcast action is BluetoothAdapter.ACTION_STATE_CHANGED
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                // Get the Bluetooth state from the intent
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                // Convert the Bluetooth state into human-readable text
                val stateText = when (state) {
                    BluetoothAdapter.STATE_OFF -> "Off"
                    BluetoothAdapter.STATE_TURNING_ON -> "Turning On"
                    BluetoothAdapter.STATE_ON -> "On"
                    BluetoothAdapter.STATE_TURNING_OFF -> "Turning Off"
                    else -> "Unknown State"
                }

                // Log the Bluetooth state change
                Log.d("BluetoothReceiver", "$stateText")

                // Create a local broadcast intent with action "com.example.BLUETOOTH_STATE_CHANGED"
                val localIntent = Intent("com.example.BLUETOOTH_STATE_CHANGED")
                // Put the Bluetooth state information as an extra in the intent
                localIntent.putExtra("bluetoothState", stateText)

                // Use LocalBroadcastManager to send an ordered broadcast
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(localIntent)
            }

            // Check if the broadcast action is BluetoothDevice.ACTION_ACL_CONNECTED
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                // A Bluetooth device is connected
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val connectedDeviceName = device?.name ?: "Unknown Device"
                Log.d("BluetoothReceiver", "Connected to device: $connectedDeviceName")
//                // Create a local broadcast intent with action "com.example.BLUETOOTH_DEVICE_CONNECTED"
                val localIntent = Intent("com.example.BLUETOOTH_DEVICE_CONNECTED")
//                // Put the connected device information as an extra in the intent
//                localIntent.putExtra("deviceInfo", )
//
//                // Use LocalBroadcastManager to send an ordered broadcast
//                LocalBroadcastManager.getInstance(context!!).sendBroadcast(localIntent)
            }
        }
    }
    fun isBluetoothOn(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.state == BluetoothAdapter.STATE_ON
    }
    // Method to get the connected Bluetooth device's name

}