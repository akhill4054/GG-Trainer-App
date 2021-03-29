package com.example.trainyourglove.data.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.trainyourglove.data.db.AppDatabase
import com.example.trainyourglove.data.db.entities.Gesture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GesturesRepository private constructor(application: Application) {

    private val _gesturesDao = AppDatabase.getInstance(application).gesturesDao()

    private val _syncedGestures by lazy { MutableLiveData<List<Gesture>>() }

    val syncedGesture: LiveData<List<Gesture>> by lazy {
        // Query synced gestures when requested for the first time
        GlobalScope.launch(Dispatchers.IO) {
            _syncedGestures.postValue(_gesturesDao.getSyncedGestures())
        }
        _syncedGestures
    }

    private val _syncGestures by lazy { MutableLiveData<List<Gesture>>() }

    val syncGestures: LiveData<List<Gesture>> by lazy {
        // Query recorded gestures when requested for the first time
        GlobalScope.launch(Dispatchers.IO) {
            _syncGestures.postValue(_gesturesDao.getRecordedGestures())
        }
        _syncGestures
    }

    suspend fun insert(gesture: Gesture) {
        withContext(Dispatchers.IO) {
            // Insert
            _gesturesDao.insert(gesture)
            // Notify active observers
            updateOnChange(INSERT, gesture)
        }
    }

    suspend fun update(gesture: Gesture) {
        withContext(Dispatchers.IO) {
            // Update
            _gesturesDao.update(gesture)
            // Notify active observers
            updateOnChange(UPDATE, gesture)
        }
    }

    suspend fun remove(gesture: Gesture) {
        withContext(Dispatchers.IO) {
            // Update
            _gesturesDao.remove(gesture)
            // Notify active observers
            updateOnChange(REMOVE, gesture)
        }
    }

    private suspend fun updateOnChange(change: Int, gesture: Gesture) {
        when (change) {
            INSERT -> {
                // As newly inserted gestures are not synced,
                // only update sync gestures.
                updateSyncGestures()
            }
            UPDATE -> {
                if (gesture.syncStatus == Gesture.SYNC_STATUS_SYNC) {
                    updateSyncGestures()
                } else {
                    updateSyncedGestures()
                }
            }
            else -> { // REMOVE
                if (gesture.syncStatus == Gesture.SYNC_STATUS_SYNC) {
                    updateSyncGestures()
                } else {
                    updateSyncedGestures()
                }
            }
        }
    }

    private suspend fun updateSyncGestures() {
        if (syncGestures.hasObservers()) {
            _syncGestures.postValue(_gesturesDao.getRecordedGestures())
        }
    }

    private suspend fun updateSyncedGestures() {
        if (syncedGesture.hasObservers()) {
            _syncedGestures.postValue(_gesturesDao.getSyncedGestures())
        }
    }

    companion object {
        private const val INSERT = 0
        private const val UPDATE = 1
        private const val REMOVE = 2

        @Volatile
        private var INSTANCE: GesturesRepository? = null

        fun getInstance(application: Application): GesturesRepository {
            var localRef = INSTANCE

            return localRef ?: synchronized(GesturesRepository::class.java) {
                localRef = INSTANCE
                localRef ?: GesturesRepository(application).also {
                    INSTANCE = it
                }
            }
        }
    }
}