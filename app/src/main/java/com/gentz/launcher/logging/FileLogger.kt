package com.gentz.launcher.logging

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private const val LOG_FILE_NAME = "app_debug_logs.txt"
    private const val MAX_LOG_SIZE_BYTES = 2L * 1024L * 1024L
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    private fun getLogFile(context: Context): File {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(dir, LOG_FILE_NAME)
    }

    @JvmStatic
    @Synchronized
    fun log(context: Context, message: String) {
        runCatching {
            appendLine(context, "[${dateFormat.format(Date())}] $message\n")
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
            appendLine(context, text)
        }
    }

    @JvmStatic
    @Synchronized
    fun logRaw(context: Context, message: String) {
        runCatching {
            appendLine(context, "$message\n")
        }
    }

    private fun appendLine(context: Context, text: String) {
        val file = getLogFile(context)
        if (file.exists() && file.length() >= MAX_LOG_SIZE_BYTES) {
            file.writeText("[${dateFormat.format(Date())}] Log rotated (size limit reached)\n")
        }
        file.appendText(text)
    }
}
