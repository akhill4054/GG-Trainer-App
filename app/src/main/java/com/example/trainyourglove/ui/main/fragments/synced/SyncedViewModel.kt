package com.example.trainyourglove.ui.main.fragments.synced

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.repositories.GesturesRepository

class SyncedViewModel(application: Application) : AndroidViewModel(application) {

    private val _gesturesRepository = GesturesRepository.getInstance(application)

    val syncedGestures: LiveData<List<Gesture>> = _gesturesRepository.syncedGesture
}