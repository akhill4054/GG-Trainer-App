package com.example.trainyourglove.ui.main.fragments.sync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.repositories.GesturesRepository
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : AndroidViewModel(application) {

    private val _gesturesRepository = GesturesRepository.getInstance(application)

    val syncGestures: LiveData<List<Gesture>> = _gesturesRepository.syncGestures

    fun deleteGesture(gesture: Gesture) {
        viewModelScope.launch {
            _gesturesRepository.remove(gesture)
        }
    }
}