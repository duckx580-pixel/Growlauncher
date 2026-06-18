package com.gentz.launcher.logging

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val LOG_FILE_NAME = "app_debug_logs.txt"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private fun getLogFile(context: Context): File {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(dir, LOG_FILE_NAME)
    }

    @JvmStatic
    @Synchronized
    fun log(context: Context, message: String) {
        runCatching {
            val line = "[${dateFormat.format(Date())}] $message\n"
            getLogFile(context).appendText(line)
        }
    }

    @JvmStatic
    @Synchronized
    fun logException(context: Context, tag: String, throwable: Throwable) {
        runCatching {
            val text = buildString {
                append("[${dateFormat.format(Date())}] [$tag] ${throwable::class.java.name}: ${throwable.message}\n")
                append(throwable.stackTraceToString())
                append("\n\n")
            }
            getLogFile(context).appendText(text)
        }
    }
}
