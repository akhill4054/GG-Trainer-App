package com.example.trainyourglove.ui.main.recorded.synced

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.models.SyncedGesture
import com.example.trainyourglove.data.repositories.GesturesRepository
import com.example.trainyourglove.data.repositories.NetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncedViewModel(application: Application) : AndroidViewModel(application) {

    private val _netRep = NetRepository.getInstance(application)

    private val _gesturesRepository = GesturesRepository.getInstance(application)

    val syncedGestures: LiveData<List<Gesture>> = _gesturesRepository.syncedGesture

    init {
        // Get synced gestures over network
        _netRep.getSyncedGestures(object : NetRepository.Callback<List<SyncedGesture>> {
            override fun onData(data: List<SyncedGesture>?) {
                data?.let { syncedGestures ->
                    // Replace synced gestures in local db
                    viewModelScope.launch(Dispatchers.IO) {
                        // Remove local synced gestures
                        _gesturesRepository.removeSyncedGestures()

                        val localSyncedGestures = mutableListOf<Gesture>()

                        for (gesture in syncedGestures) {
                            val localGesture = gesture.toGesture(getApplication())
                            localSyncedGestures.add(localGesture)
                        }

                        // Store in db
                        _gesturesRepository.insertGestures(localSyncedGestures)
                    }
                }
            }
        })
    }

    fun unsync(gesture: Gesture): LiveData<Boolean> {
        // Remove from db
        viewModelScope.launch(Dispatchers.IO) {
            _gesturesRepository.update(gesture.copy(syncStatus = Gesture.SYNC_STATUS_SYNC))
        }
        val unsyncRequest = MutableLiveData<Boolean>()
        _netRep.remove(gesture.mappedText, object : NetRepository.Callback<String> {
            override fun onData(data: String?) {
                unsyncRequest.postValue(data != null)
            }
        })
        return unsyncRequest
    }
}