package com.example.batterysaver_alert.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.batterysaver_alert.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()// hide the action bar
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