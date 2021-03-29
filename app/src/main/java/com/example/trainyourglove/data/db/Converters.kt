package com.example.trainyourglove.data.db

import androidx.room.TypeConverter
import com.example.trainyourglove.data.db.entities.Gesture

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: Gesture.SyncStatus): Int = status.toInt()

    @TypeConverter
    fun toSyncStatus(status: Int): Gesture.SyncStatus =
        Gesture.SyncStatus.fromInt(status)
}