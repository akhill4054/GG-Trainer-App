package com.example.trainyourglove.utils

import android.app.Application
import com.example.trainyourglove.RECORD_FILE_PREFIX
import com.example.trainyourglove.data.PreferenceManager
import java.io.File

class FileUtils {
    companion object {
        fun createNewRecordFile(application: Application): File {
            val prefs = PreferenceManager.getInstance(application)
            val primaryKey = prefs.getMaintanedPrimaryKey()

            val dir = application.filesDir
            return File(dir, "$RECORD_FILE_PREFIX-$primaryKey")
        }
    }
}