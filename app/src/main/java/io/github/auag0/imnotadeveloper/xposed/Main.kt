package io.github.auag0.imnotadeveloper.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import io.github.auag0.imnotadeveloper.BuildConfig
import io.github.auag0.imnotadeveloper.common.Logger.logD
import io.github.auag0.imnotadeveloper.common.Logger.logE
import java.lang.reflect.Method

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
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SERVICE_ADB_TCP_PORT
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_RO_ADB_SECURE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_SYS_USB_ADB_DISABLED
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_PERSIST_SERVICE_ADB_ENABLE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_RO_DEBUGGABLE
import io.github.auag0.imnotadeveloper.common.PrefKeys.HIDE_RO_SECURE
import io.github.auag0.imnotadeveloper.common.PropKeys
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.ADB_WIFI_ENABLED
import io.github.auag0.imnotadeveloper.common.PropKeys.DEVELOPMENT_SETTINGS_ENABLED

class Main : IXposedHookLoadPackage {
    private val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

    private fun buildOverrides(): Map<String, String> {
        val map = mutableMapOf<String, String>()

        if (getSPBool(HIDE_SYS_USB_FFS_READY, true))
            map[PropKeys.SYS_USB_FFS_READY] = "0"

        if (getSPBool(HIDE_SYS_USB_CONFIG, true))
            map[PropKeys.SYS_USB_CONFIG] = "mtp"

        if (getSPBool(HIDE_PERSIST_SYS_USB_CONFIG, true))
            map[PropKeys.PERSIST_SYS_USB_CONFIG] = "mtp"

        if (getSPBool(HIDE_SYS_USB_STATE, true))
            map[PropKeys.SYS_USB_STATE] = "mtp"

        if (getSPBool(HIDE_INIT_SVC_ADBD, true))
            map[PropKeys.INIT_SVC_ADBD] = "stopped"

        if (getSPBool(HIDE_SYS_USB_ADB_DISABLED, true))
            map[PropKeys.SYS_USB_ADB_DISABLED] = "1"

        if (getSPBool(HIDE_PERSIST_SERVICE_ADB_ENABLE, true))
            map[PropKeys.PERSIST_SERVICE_ADB_ENABLE] = "0"

        if (getSPBool(HIDE_SERVICE_ADB_TCP_PORT, true))
            map[PropKeys.SERVICE_ADB_TCP_PORT] = "-1"

        if (getSPBool(HIDE_RO_ADB_SECURE, true))
            map[PropKeys.RO_ADB_SECURE] = "1"

        if (getSPBool(HIDE_RO_DEBUGGABLE, true))
            map[PropKeys.RO_DEBUGGABLE] = "0"

        if (getSPBool(HIDE_RO_SECURE, true))
            map[PropKeys.RO_SECURE] = "1"

        return map
    }

    val propOverrides get() = buildOverrides()

    override fun handleLoadPackage(param: LoadPackageParam) {
        hookSettingsMethods(param.classLoader)
        hookSystemPropertiesMethods(param.classLoader)
        hookProcessMethods(param.classLoader)
        hookNativeMethods()
    }

    private fun hookNativeMethods() {
        if (!getSPBool(HIDE_DEBUG_PROPERTIES_IN_NATIVE, true)) return
        try {
            System.loadLibrary("ImNotADeveloper")
            NativeFun.setProps(propOverrides)
        } catch (e: Exception) {
            logE(e.message)
        }
    }

    private fun hookProcessMethods(classLoader: ClassLoader) {
        if (!getSPBool(HIDE_DEBUG_PROPERTIES, true)) return
        val hookCmd = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                hookedLog(param)
                val cmdarray = (param.args[0] as Array<*>).filterIsInstance<String>()
                val firstCmd = cmdarray.getOrNull(0)
                val secondCmd = cmdarray.getOrNull(1)
                if (firstCmd == "getprop" && propOverrides.containsKey(secondCmd)) {
                    val writableCmdArray = ArrayList(cmdarray)
                    writableCmdArray[1] = "Dummy${System.currentTimeMillis()}"
                    val a: Array<String> = writableCmdArray.toTypedArray()
                    param.args[0] = a
                }
            }
        }
        val processImpl = findClass("java.lang.ProcessImpl", classLoader)
        hookAllMethods(processImpl, "start", hookCmd)

        val processManager = findClass("java.lang.ProcessManager", classLoader)
        hookAllMethods(processManager, "exec", hookCmd)
    }

    private fun hookSystemPropertiesMethods(classLoader: ClassLoader) {
        if (!getSPBool(HIDE_DEBUG_PROPERTIES, true)) return
        val methods = arrayOf(
            "native_get",
            "native_get_int",
            "native_get_long",
            "native_get_boolean"
        )
        val systemProperties = findClass("android.os.SystemProperties", classLoader)
        methods.forEach { methodName ->
            hookAllMethods(systemProperties, methodName, object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    if (param.args[0] !is String) return param.invokeOriginalMethod()
                    hookedLog(param)
                    val key = param.args[0] as String
                    val method = param.method as Method

                    val value = propOverrides[key]
                    if (value != null) {
                        return try {
                            when (method.returnType) {
                                String::class.java -> value
                                Int::class.java -> value.toInt()
                                Long::class.java -> value.toLong()
                                Boolean::class.java -> value.toBoolean()
                                else -> param.invokeOriginalMethod()
                            }
                        } catch (e: NumberFormatException) {
                            logE(e.message)
                        }
                    }

                    return param.invokeOriginalMethod()
                }
            })
        }
    }

    private fun hookSettingsMethods(classLoader: ClassLoader) {
        val bannedKeys = ArrayList<String>()
        if (getSPBool(HIDE_DEVELOPER_MODE, true)) bannedKeys.add(DEVELOPMENT_SETTINGS_ENABLED)
        if (getSPBool(HIDE_USB_DEBUG, true)) bannedKeys.add(ADB_ENABLED)
        if (getSPBool(HIDE_WIRELESS_DEBUG, true)) bannedKeys.add(ADB_WIFI_ENABLED)
        if (bannedKeys.isEmpty()) return
        val settingsClassNames = arrayOf(
            "android.provider.Settings.Secure",
            "android.provider.Settings.System",
            "android.provider.Settings.Global",
            "android.provider.Settings.NameValueCache"
        )
        settingsClassNames.forEach {
            val clazz = findClass(it, classLoader)
            hookAllMethods(clazz, "getStringForUser", object : XC_MethodReplacement() {
                override fun replaceHookedMethod(param: MethodHookParam): Any? {
                    hookedLog(param)
                    val name = param.args[1] as? String?
                    return if (bannedKeys.contains(name)) {
                        "0"
                    } else {
                        param.invokeOriginalMethod()
                    }
                }
            })
        }
    }

    private fun hookedLog(param: MethodHookParam) {
        val method = param.method as Method
        val message = buildString {
            appendLine("Hooked ${method.declaringClass.name}$${param.method.name} -> ${method.returnType.name}")
            param.args.forEachIndexed { index, any: Any? ->
                appendLine("    $index:${any.string()}")
            }
        }
        logD(message)
    }

    private fun Any?.string(): String {
        return when (this) {
            is List<*> -> joinToString(prefix = "[", postfix = "]")
            is Array<*> -> joinToString(prefix = "[", postfix = "]")
            else -> toString()
        }
    }

    private fun String.toBoolean(): Boolean {
        return when {
            equals("true", true) || equals("1", true) -> true
            equals("false", true) || equals("0", true) -> false
            else -> throw NumberFormatException(this)
        }
    }

    private fun MethodHookParam.invokeOriginalMethod(): Any? {
        return XposedBridge.invokeOriginalMethod(method, thisObject, args)
    }

    private fun getSPBool(key: String, def: Boolean): Boolean {
        prefs.reload()
        return prefs.getBoolean(key, def)
    }
}