package com.example.batterysaver_alert.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.batterysaver_alert.R

class SettingActivity : AppCompatActivity() {
    private lateinit var percen_spinner: Spinner
    private lateinit var time_chooser_spinner: Spinner
    private lateinit var heat_chooser_spinner: Spinner
    private lateinit var noti_text_switch : Switch
    private lateinit var noti_sound_switch : Switch
    private  var noti_text_setting_check : Boolean = false
    private var noti_sound_setting_check : Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.hide()// hide the action bar
        // get ids
        noti_text_switch = findViewById(R.id.noti_switch)
        noti_sound_switch = findViewById(R.id.noti_sound_switch)
        percen_spinner = findViewById(R.id.low_rate_spinner)
        time_chooser_spinner = findViewById(R.id.off_time_spinner)
        heat_chooser_spinner = findViewById(R.id.heat_limit_spinner)

        // get string arrays
        val low_percent_array = resources.getStringArray(R.array.low_percent_arrray)// get array text
        val time_chooser_array = resources.getStringArray(R.array.time_chooser_arrray)// get array text
        val heat_limit_array = resources.getStringArray(R.array.heat_limit_chooser_arrray)
        // create adapter
        val adapter = ArrayAdapter(this, R.layout.spinner_item_custom, low_percent_array)// create adapter
        val adapter2 =ArrayAdapter(this,R.layout.spinner_item_custom, time_chooser_array)
        val adapter3 =ArrayAdapter(this,R.layout.spinner_item_custom, heat_limit_array)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter

        percen_spinner.adapter = adapter
        time_chooser_spinner.adapter = adapter2
        heat_chooser_spinner.adapter = adapter3

        //get shared preference setting
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        noti_text_setting_check = sharedPreferences.getBoolean("noti_text",false)
        noti_sound_setting_check = sharedPreferences.getBoolean("noti_sound",false)


        // set saved limits in setting
        setSelectedPercentLimit(sharedPreferences,low_percent_array)// get what user had save and set it to spinner
        setSelectedHeatLimit(sharedPreferences, heat_limit_array)// get what user had save and set it to spinner
        //
        percen_spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = low_percent_array[position]
                Log.v("select",selectedItem)
                sharedPreferences.edit()?.putString("percent_limit",selectedItem)?.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })

        //
        heat_chooser_spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = heat_limit_array[position]
                Log.v("select heat",selectedItem)
                sharedPreferences.edit()?.putString("heat_limit",selectedItem)?.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        })
    }
    override fun onStart() {
        super.onStart()

        // call set switch func
        setSwitch()
    }
    //set spinner choice for percent limit
    fun setSelectedPercentLimit(sharedPreferences: SharedPreferences,stringArray: Array<String>){
        val percent_limit_t = sharedPreferences.getString("percent_limit","20%")
        var location_from_arr = 0
        if(percent_limit_t in stringArray){
            location_from_arr = stringArray.indexOf(percent_limit_t)
        }
        percen_spinner.setSelection(location_from_arr)
    }
    //set spinner choice for percent limit
    fun setSelectedHeatLimit(sharedPreferences: SharedPreferences,stringArray: Array<String>){
        val heat_limit_t = sharedPreferences.getString("heat_limit","40°C")
        var location_from_arr = 0
        if(heat_limit_t in stringArray){
            location_from_arr = stringArray.indexOf(heat_limit_t)
        }
        heat_chooser_spinner.setSelection(location_from_arr)
    }
    // set switch func
    fun setSwitch(){
        if(noti_text_setting_check){
            noti_text_switch.toggle()
        }
        if(noti_sound_setting_check){
            noti_sound_switch.toggle()
        }
    }

    // set switch function to send notification text on/off to shared preference
    fun switchNotiTextFunc(view: View) {
        noti_text_setting_check = !noti_text_setting_check
        // Save the switch value to shared preferences
        val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putBoolean("noti_text",noti_text_setting_check)?.apply()
    }

    // set switch function to send notification sound on/off to shared preference
    fun switchNotiSoundFunc(view: View) {
        noti_sound_setting_check = !noti_sound_setting_check
        // Save the unplug time to shared preferences
        val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putBoolean("noti_sound", noti_sound_setting_check)?.apply()
    }
    // button func 1
    fun openHistoryFromSetting(view: View) {
        val intent = Intent(this,HistoryActivity::class.java)
        startActivity(intent)
    }
    //button func 2
    fun openHomeFromSetting(view: View) {
        val intent = Intent(this,HomeActivity::class.java)
        startActivity(intent)
    }


}