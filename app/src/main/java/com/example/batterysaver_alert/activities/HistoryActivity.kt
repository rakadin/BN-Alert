package com.example.batterysaver_alert.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.batterysaver_alert.R

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.hide()// hide the action bar

    }

    //button func 1
    fun openHomeFromHistory(view: View) {
        val intent = Intent(this,HomeActivity::class.java)
        startActivity(intent)
    }
    // button func 2
    fun openSettingFromHistory(view: View) {
        val intent = Intent(this,SettingActivity::class.java)
        startActivity(intent)
    }
}