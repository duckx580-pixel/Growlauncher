package com.gentz.launcher.logging

import android.content.Context
import android.os.Process
import kotlin.system.exitProcess

object CrashLogger {
    @Volatile
    private var initialized = false

    @JvmStatic
    fun init(context: Context) {
        if (initialized) return

        synchronized(this) {
            if (initialized) return

            val appContext = context.applicationContext
            val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                FileLogger.logException(
                    appContext,
                    "UNCAUGHT_EXCEPTION thread=${thread.name}",
                    throwable
                )

                if (previousHandler != null) {
                    previousHandler.uncaughtException(thread, throwable)
                } else {
                    Process.killProcess(Process.myPid())
                    exitProcess(10)
                }
            }

            initialized = true
        }
    }
}
