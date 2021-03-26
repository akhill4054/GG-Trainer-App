package com.example.trainyourglove.utils

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.trainyourglove.BuildConfig
import java.io.File

class AppLogger(private val application: Application) {

    private val logFile: File by lazy { File(application.cacheDir, "DEBUG-LOGS.txt") }

    init {
        if (logFile.exists()) logFile.delete()
    }

    private var lineNumber = 0

    fun log(message: String) {
        if (!logFile.exists()) {
            logFile.createNewFile()
        }

        logFile.appendText("${++lineNumber}? : $message\n")
    }

    fun shareActiveLog(activity: Activity) {
        val logFileUri: Uri = FileProvider.getUriForFile(
            application,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            logFile
        )

        val shareIntent = Intent()

        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Temp permission for receiving app to read this file
        shareIntent.setDataAndType(logFileUri, application.contentResolver.getType(logFileUri))
        shareIntent.putExtra(Intent.EXTRA_STREAM, logFileUri)

        try {
            shareIntent.setPackage("com.whatsapp")

            activity.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(application, "WhatsApp not found.", Toast.LENGTH_SHORT).show()

            activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }

    companion object {
        @Volatile
        private var mInstance: AppLogger? = null

        fun getInstance(application: Application) = synchronized(AppLogger::class.java) {
            mInstance ?: AppLogger(application).apply {
                mInstance = this
            }
        }
    }
}