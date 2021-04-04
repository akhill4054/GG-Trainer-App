package com.example.trainyourglove.data.models

import android.app.Application
import androidx.core.net.toUri
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.utils.AppFileUtils
import com.google.gson.annotations.SerializedName

class SyncedGesture(
    @SerializedName("data")
    val data: String,
    @SerializedName("mapped_text")
    val mappedText: String,
    @SerializedName("rd")
    val durationMillis: Long
) {
    fun toGesture(application: Application): Gesture {
        // Save file
        val rf = AppFileUtils.createNewRecordFile(application)
        // Write data to file
        rf.writeText(data)

        return Gesture(
            mappedText = mappedText,
            dataFileUri = rf.toUri().toString(),
            durationMillis = durationMillis,
            syncStatus = Gesture.SYNC_STATUS_SYNCED
        )
    }
}