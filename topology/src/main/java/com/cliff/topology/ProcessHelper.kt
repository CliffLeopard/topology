package com.cliff.topology

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import java.lang.reflect.Method


/**
 * author:CliffLeopard
 * date:2022/3/7
 * time:19:42
 * email:precipiceleopard@gmail.com
 * link:
 */
object ProcessHelper {
    private var processName: String? = null
    fun currentProcessName(context: Context): String? {
        if (!TextUtils.isEmpty(processName)) {
            return processName
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            processName = getCurrentProcessNameByApplication()
        }
        if (TextUtils.isEmpty(processName)) {
            processName = getCurrentProcessNameByActivityThread()
        }
        return processName
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getCurrentProcessNameByApplication(): String {
        return Application.getProcessName()
    }

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    fun getCurrentProcessNameByActivityThread(): String? {
        var processName: String? = null
        try {
            val clz = Class.forName("android.app.ActivityThread", false, Application::class.java.classLoader)
            val declaredMethod: Method = clz.getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke: Any? = declaredMethod.invoke(null, arrayOfNulls<Any>(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (ignore: Throwable) {
        }
        return processName
    }
}