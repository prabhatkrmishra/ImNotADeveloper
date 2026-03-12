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
import java.io.File

import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEBUG_PROPERTIES_IN_NATIVE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_DEVELOPER_MODE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_USB_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_WIRELESS_DEBUG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SYS_USB_STATE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SYS_USB_CONFIG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_PERSIST_SYS_USB_CONFIG
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SYS_USB_FFS_READY
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_INIT_SVC_ADBD
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SYS_USB_ADB_DISABLED
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_PERSIST_SERVICE_ADB_ENABLE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SERVICE_ADB_TCP_PORT
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_RO_ADB_SECURE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_RO_DEBUGGABLE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_RO_SECURE

class SettingsActivity : Activity() {
    private val options by lazy {
        listOf(
            Option(getString(R.string.hide_developer_mode), HIDE_DEVELOPER_MODE, true),
            Option(getString(R.string.hide_usb_debug), HIDE_USB_DEBUG, true),
            Option(getString(R.string.hide_wireless_debug), HIDE_WIRELESS_DEBUG, true),

            Option(getString(R.string.hide_usb_state), HIDE_SYS_USB_STATE, true),
            Option(getString(R.string.hide_usb_config), HIDE_SYS_USB_CONFIG, true),
            Option(getString(R.string.hide_persist_usb_config), HIDE_PERSIST_SYS_USB_CONFIG, true),
            Option(getString(R.string.hide_usb_ffs_ready), HIDE_SYS_USB_FFS_READY, true),
            Option(getString(R.string.hide_init_svc_adbd), HIDE_INIT_SVC_ADBD, true),
            Option(getString(R.string.hide_sys_usb_adb_disabled), HIDE_SYS_USB_ADB_DISABLED, true),
            Option(getString(R.string.hide_persist_service_adb_enable), HIDE_PERSIST_SERVICE_ADB_ENABLE, true),

            Option(getString(R.string.hide_adb_tcp_port), HIDE_SERVICE_ADB_TCP_PORT, true),

            Option(getString(R.string.hide_ro_adb_secure), HIDE_RO_ADB_SECURE, true),
            Option(getString(R.string.hide_ro_debuggable), HIDE_RO_DEBUGGABLE, true),
            Option(getString(R.string.hide_ro_secure), HIDE_RO_SECURE, true),

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