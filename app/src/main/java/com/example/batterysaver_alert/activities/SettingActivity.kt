package com.example.batterysaver_alert.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.batterysaver_alert.R

class SettingActivity : AppCompatActivity() {
    private lateinit var percen_spinner: Spinner
    private lateinit var time_chooser_spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportActionBar?.hide()// hide the action bar
        // get ids
        percen_spinner = findViewById(R.id.low_rate_spinner)
        time_chooser_spinner = findViewById(R.id.off_time_spinner)

        val low_percent_array = resources.getStringArray(R.array.low_percent_arrray)// get array text
        val time_chooser_array = resources.getStringArray(R.array.time_chooser_arrray)// get array text

        val adapter = ArrayAdapter(this, R.layout.spinner_item_custom, low_percent_array)// create adapter
        val adapter2 =ArrayAdapter(this,R.layout.spinner_item_custom, time_chooser_array)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter
        percen_spinner.adapter = adapter
        time_chooser_spinner.adapter = adapter2
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