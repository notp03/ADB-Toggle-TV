
package com.local.adbtoggle

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.database.ContentObserver
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var btnOn: Button
    private lateinit var btnOff: Button

    private val settingKey = "adb_wifi_enabled"

    private val settingsObserver = object : ContentObserver(
        Handler(Looper.getMainLooper())
    ) {
        override fun onChange(selfChange: Boolean) {
            updateStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(18, 18, 18))
            setPadding(64, 64, 64, 64)
        }

        statusText = TextView(this).apply {
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        btnOn = Button(this).apply {
            text = "TURN WIRELESS DEBUGGING ON"
            textSize = 18f
            isFocusable = true
            setOnClickListener { setWifiAdb(true) }
        }

        btnOff = Button(this).apply {
            text = "TURN WIRELESS DEBUGGING OFF"
            textSize = 18f
            isFocusable = true
            setOnClickListener { setWifiAdb(false) }
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

    override fun onResume() {
        super.onResume()

        contentResolver.registerContentObserver(
            Settings.Global.getUriFor(settingKey),
            false,
            settingsObserver
        )

        updateStatus()
    }

    override fun onPause() {
        contentResolver.unregisterContentObserver(settingsObserver)
        super.onPause()
    }

    private fun isWifiConnected(): Boolean {
        val cm = getSystemService(
            CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network)
            ?: return false

        return capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        )
    }

    private fun setWifiAdb(enabled: Boolean) {
        try {
            if (enabled && !isWifiConnected()) {
                Toast.makeText(
                    this,
                    "Connect to Wi-Fi first!",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val state = if (enabled) 1 else 0

            val success = Settings.Global.putInt(
                contentResolver,
                settingKey,
                state
            )

            updateStatus()

            if (success) {
                val message = if (enabled) {
                    "Wireless debugging ON"
                } else {
                    "Wireless debugging OFF"
                }

                Toast.makeText(
                    this,
                    message,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Failed to change setting!",
                    Toast.LENGTH_LONG
                ).show()
            }

        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "WRITE_SECURE_SETTINGS permission missing!",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateStatus() {
        try {
            val current = Settings.Global.getInt(
                contentResolver,
                settingKey,
                0
            )

            if (current == 1) {
                statusText.text = "WIRELESS DEBUGGING: ON"
                statusText.setTextColor(
                    Color.rgb(76, 175, 80)
                )
            } else {
                statusText.text = "WIRELESS DEBUGGING: OFF"
                statusText.setTextColor(
                    Color.rgb(244, 67, 54)
                )
            }

        } catch (e: Exception) {
            statusText.text = "STATUS UNAVAILABLE"
            statusText.setTextColor(Color.YELLOW)
        }
    }
}
