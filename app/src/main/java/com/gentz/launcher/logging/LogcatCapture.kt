package com.gentz.launcher.logging

import android.content.Context
import kotlin.concurrent.thread

object LogcatCapture {
    @Volatile
    private var started = false

    @JvmStatic
    fun start(context: Context) {
        if (started) return

        synchronized(this) {
            if (started) return
            started = true
        }

        val appContext = context.applicationContext
        thread(start = true, isDaemon = true, name = "logcat-file-capture") {
            runCatching {
                FileLogger.log(appContext, "Starting full logcat capture")
                val process = ProcessBuilder(
                    "logcat",
                    "-v",
                    "threadtime",
                    "-b",
                    "all",
                    "*:V"
                ).redirectErrorStream(true).start()

                process.inputStream.bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        FileLogger.logRaw(appContext, line)
                    }
                }
            }.onFailure { throwable ->
                FileLogger.logException(appContext, "LOGCAT_CAPTURE_FAILED", throwable)
            }
        }
    }
}
