package com.example.trainyourglove.utils

import android.app.Application
import com.example.trainyourglove.RECORDING_FILE_PREFIX
import com.example.trainyourglove.data.PreferenceManager
import java.io.File
import java.net.URI

class AppFileUtils {
    companion object {
        fun createNewRecordFile(application: Application): File {
            val prefs = PreferenceManager.getInstance(application)
            val primaryKey = prefs.getMaintanedPrimaryKey()

            val dir = application.filesDir

            val file = File(dir, "$RECORDING_FILE_PREFIX-$primaryKey.txt")
            if (file.exists()) file.delete() // For safety
            file.createNewFile()

            return file
        }

        fun openFile(uri: String): File {
            return File(URI.create(uri))
        }
    }
}