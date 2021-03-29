package com.example.trainyourglove.data.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.trainyourglove.data.db.entities.Gesture

@Dao
interface GesturesDao {
    @Insert
    suspend fun insert(gesture: Gesture)

    @Delete
    suspend fun remove(gesture: Gesture)

    @Query("SELECT * FROM gestures WHERE syncStatus LIKE ${Gesture.SyncStatus.SYNC_AS_INT} ORDER BY id ASC")
    suspend fun getRecordedGestures(): List<Gesture>

    @Query("SELECT * FROM gestures WHERE syncStatus LIKE ${Gesture.SyncStatus.SYNCED_AS_INT} ORDER BY id DESC")
    suspend fun getSyncedGestures(): List<Gesture>
}