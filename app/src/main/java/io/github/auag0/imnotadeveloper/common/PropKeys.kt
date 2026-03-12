package io.github.auag0.imnotadeveloper.common

object PropKeys {
    const val DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled"
    const val ADB_ENABLED = "adb_enabled"
    const val ADB_WIFI_ENABLED = "adb_wifi_enabled"

    const val SYS_USB_FFS_READY = "sys.usb.ffs.ready"
    const val SYS_USB_CONFIG = "sys.usb.config"
    const val PERSIST_SYS_USB_CONFIG = "persist.sys.usb.config"
    const val SYS_USB_STATE = "sys.usb.state"
    const val INIT_SVC_ADBD = "init.svc.adbd"
    const val SYS_USB_ADB_DISABLED = "sys.usb.adb.disabled"
    const val PERSIST_SERVICE_ADB_ENABLE = "persist.service.adb.enable"

    const val SERVICE_ADB_TCP_PORT = "service.adb.tcp.port"

    const val RO_ADB_SECURE = "ro.adb.secure"
    const val RO_DEBUGGABLE = "ro.debuggable"
    const val RO_SECURE = "ro.secure"
}