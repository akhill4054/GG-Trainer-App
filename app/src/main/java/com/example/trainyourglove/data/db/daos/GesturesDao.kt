package com.example.trainyourglove.data.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.db.entities.Gesture.Companion.SYNC_STATUS_SYNC
import com.example.trainyourglove.data.db.entities.Gesture.Companion.SYNC_STATUS_SYNCED

@Dao
interface GesturesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gesture: Gesture)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(gestures: List<Gesture>)

    @Update
    suspend fun update(gesture: Gesture)

    @Delete
    suspend fun remove(gesture: Gesture)

    @Query("DELETE FROM gestures WHERE syncStatus = :syncStatus")
    suspend fun removeGestures(syncStatus: Int)

    @Query("SELECT * FROM gestures WHERE syncStatus LIKE $SYNC_STATUS_SYNC ORDER BY id ASC")
    suspend fun getRecordedGestures(): List<Gesture>

    @Query("SELECT * FROM gestures WHERE syncStatus LIKE $SYNC_STATUS_SYNCED ORDER BY id DESC")
    suspend fun getSyncedGestures(): List<Gesture>

    @Query("SELECT * FROM gestures ORDER BY id DESC")
    fun getGestures(): LiveData<List<Gesture>>
}