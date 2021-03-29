package com.example.trainyourglove.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gestures")
data class Gesture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mappedText: String,
    val dataFileUri: String,
    val syncStatus: Int = SYNC_STATUS_SYNC,
    val durationMillis: Long
) {
    companion object {
        const val SYNC_STATUS_SYNC = 0
        const val SYNC_STATUS_SYNCED = 1
    }
}