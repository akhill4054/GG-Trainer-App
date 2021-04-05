package com.example.trainyourglove.ui.main.recorded.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.repositories.GesturesRepository
import com.example.trainyourglove.data.repositories.NetRepository
import com.example.trainyourglove.utils.AppFileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val _netRepo = NetRepository.getInstance(application)

    private val _gesturesRepository = GesturesRepository.getInstance(application)

    val syncGestures: LiveData<List<Gesture>> = _gesturesRepository.syncGestures

    fun sync(gesture: Gesture): LiveData<Boolean> {
        val syncRequest = MutableLiveData<Boolean>()

        viewModelScope.launch(Dispatchers.Default) {
            val syncedGesture = gesture.toSyncedGesture(getApplication())
            if (syncedGesture != null) {
                _netRepo.sync(syncedGesture, object : NetRepository.Callback<String> {
                    override fun onData(data: String?) {
                        viewModelScope.launch(Dispatchers.IO) {
                            _gesturesRepository.update(gesture.copy(syncStatus = Gesture.SYNC_STATUS_SYNCED))
                        }
                        syncRequest.postValue(data != null)
                    }
                })
            } else {
                // Remove the gesture from local db
                _gesturesRepository.remove(gesture)
                // Show error
                syncRequest.postValue(null)
            }
        }

        return syncRequest
    }

    fun syncAll(): LiveData<Boolean> {
        val syncRequest = MutableLiveData<Boolean>()

        viewModelScope.launch(Dispatchers.IO) {
            for (gesture in syncGestures.value!!) {
                val syncedGesture = gesture.toSyncedGesture(getApplication())

                if (syncedGesture != null) {
                    val call = _netRepo.syncSynchronously(syncedGesture)
                    try {
                        val response = call.execute()
                        if (response.code() == 200) {
                            _gesturesRepository.update(gesture.copy(syncStatus = Gesture.SYNC_STATUS_SYNCED))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // Remove the gesture from local db
                    _gesturesRepository.remove(gesture)
                    // Show error
                    syncRequest.postValue(null)
                }
            }
            syncRequest.postValue(true)
        }

        return syncRequest
    }

    fun deleteGesture(gesture: Gesture) {
        viewModelScope.launch {
            _gesturesRepository.remove(gesture)

            val recordingFile = AppFileUtils.openFile(gesture.dataFileUri)
            recordingFile.delete()
        }
    }
}