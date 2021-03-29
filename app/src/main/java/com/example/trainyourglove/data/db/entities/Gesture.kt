package com.example.trainyourglove.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gestures")
data class Gesture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mappedText: String,
    val dataFileUri: String,
    val syncStatus: SyncStatus = SyncStatus.Sync,
    val durationMillis: Long
) {
    sealed class SyncStatus {
        object Sync : SyncStatus()
        object Synced : SyncStatus()

        fun toInt(): Int {
            return if (this is Sync) SYNC_AS_INT else SYNCED_AS_INT
        }

        companion object {
            const val SYNC_AS_INT = 0
            const val SYNCED_AS_INT = 1

            fun fromInt(status: Int): SyncStatus {
                return if (status == SYNCED_AS_INT) Sync else Synced
            }
        }
    }
}