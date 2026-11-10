package com.local.adbtoggle

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(64, 64, 64, 64)
        }

        statusText = TextView(this).apply {
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        val btnOn = Button(this).apply {
            text = "Turn Wireless Debugging ON"
            textSize = 20f
            isFocusable = true
            isFocusableInTouchMode = true
            setOnClickListener { setWifiAdb(1) }
        }

        val btnOff = Button(this).apply {
            text = "Turn Wireless Debugging OFF"
            textSize = 20f
            isFocusable = true
            isFocusableInTouchMode = true
            setOnClickListener { setWifiAdb(0) }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 16, 0, 16)
        }

        layout.addView(statusText)
        layout.addView(btnOn, params)
        layout.addView(btnOff, params)

        setContentView(layout)
        updateStatus()
        btnOff.requestFocus()
    }

    private fun setWifiAdb(state: Int) {
        try {
            Settings.Global.putInt(contentResolver, "adb_wifi_enabled", state)
            updateStatus()
            val msg = if (state == 1) "Wireless ADB Enabled" else "Wireless ADB Disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Permission missing! Run: adb shell pm grant com.local.adbtoggle android.permission.WRITE_SECURE_SETTINGS",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateStatus() {
        val current = Settings.Global.getInt(contentResolver, "adb_wifi_enabled", 0)
        if (current == 1) {
            statusText.text = "Wireless ADB: ON"
            statusText.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            statusText.text = "Wireless ADB: OFF"
            statusText.setTextColor(Color.parseColor("#F44336"))
        }
    }
}
