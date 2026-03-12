package io.github.auag0.imnotadeveloper.app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Switch
import io.github.auag0.imnotadeveloper.BuildConfig
import io.github.auag0.imnotadeveloper.R
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES_IN_NATIVE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEVELOPER_MODE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_USB_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_WIRELESS_DEBUG
import java.io.File

class SettingsActivity : Activity() {
    private val options by lazy {
        listOf(
            Option(getString(R.string.hide_developer_mode), HIDE_DEVELOPER_MODE, true),
            Option(getString(R.string.hide_usb_debug), HIDE_USB_DEBUG, true),
            Option(getString(R.string.hide_wireless_debug), HIDE_WIRELESS_DEBUG, true),
            Option(getString(R.string.hide_debug_properties), HIDE_DEBUG_PROPERTIES, true),
            Option(
                getString(R.string.hide_debug_properties_in_native),
                HIDE_DEBUG_PROPERTIES_IN_NATIVE,
                true
            ),
        )
    }

    private data class Option(
        val title: String,
        val key: String,
        val defaultValue: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefName = "${BuildConfig.APPLICATION_ID}_preferences"
        val prefs = getSharedPreferences(prefName, Context.MODE_PRIVATE)

        @SuppressLint("SetWorldReadable")
        fun applyWorldReadablePermissions() {
            try {
                val dataDir = applicationInfo.dataDir
                val prefsDir = File(dataDir, "shared_prefs")
                val prefsFile = File(prefsDir, "$prefName.xml")

                prefsDir.setExecutable(true, false)
                prefsDir.setReadable(true, false)
                prefsFile.setReadable(true, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // I ensure permissions are applied initially as well
        applyWorldReadablePermissions()

        val container = LinearLayout(this).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            orientation = LinearLayout.VERTICAL
            val paddingSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                16f,
                resources.displayMetrics
            ).toInt()
            setPadding(paddingSize, paddingSize, paddingSize, paddingSize)
        }

        options.forEach { option ->
            val switch = Switch(this).apply {
                text = option.title
                textSize = 18f
                isChecked = prefs.getBoolean(option.key, option.defaultValue)
                setOnCheckedChangeListener { _, isChecked ->
                    prefs.edit().putBoolean(option.key, isChecked).apply()
                    applyWorldReadablePermissions()
                }
            }
            container.addView(switch)
        }

        setContentView(container)
        with(getColor(android.R.color.transparent)) {
            window.navigationBarColor = this
            window.statusBarColor = this
        }
    }
}