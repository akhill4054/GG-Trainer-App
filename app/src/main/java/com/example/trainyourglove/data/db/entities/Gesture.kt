package com.example.trainyourglove.data.db.entities

import android.app.Application
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.trainyourglove.data.models.SyncedGesture
import com.example.trainyourglove.utils.AppFileUtils
import java.io.IOException

@Entity(tableName = "gestures", indices = [Index(value = ["mappedText"], unique = true)])
data class Gesture(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mappedText: String,
    val dataFileUri: String,
    val syncStatus: Int = SYNC_STATUS_SYNC,
    val durationMillis: Long
) {
    companion object {
        const val SYNC_STATUS_SYNC = 0
        const val SYNC_STATUS_SYNCED = 1
    }

    fun isSynced(): Boolean {
        return syncStatus == SYNC_STATUS_SYNCED
    }

    suspend fun toSyncedGesture(application: Application): SyncedGesture? {
        val rf = AppFileUtils.openFile(dataFileUri)

        if (rf.exists()) {
            // Try to read file contents
            try {
                var data = ""
                val lines = rf.readLines()
                if (lines.size <= 3) {
                    for (l in lines) data += (l + "\n")
                } else {
                    // Read the last 5 lines
                    for (i in (lines.size - 1) downTo (lines.size - 5)) {
                        data += (lines[i] + "\n")
                    }
                }

                return SyncedGesture(
                    data = data,
                    mappedText = mappedText,
                    durationMillis = durationMillis
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return null
    }
}