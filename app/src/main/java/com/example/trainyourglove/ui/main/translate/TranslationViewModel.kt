package com.example.trainyourglove.ui.main.translate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.trainyourglove.connectivity.AppBluetooth
import com.example.trainyourglove.data.repositories.NetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    private val _netRepo = NetRepository.getInstance(application)

    val appBluetooth = AppBluetooth.getInstance()

    private val _translationState = MutableLiveData<TranslationState>()

    val translationState: LiveData<TranslationState> = _translationState

    @Volatile
    private var isTranslationPaused = false

    init {
        viewModelScope.launch(Dispatchers.Default) {
            var linesCount = 0
            var collectedData = ""
            appBluetooth.readData.collect { data ->
                if (!isTranslationPaused) {
                    if (++linesCount < 10) {
                        collectedData += data
                    } else {
                        translate(collectedData)
                        collectedData = ""
                        linesCount = 0
                    }
                } else {
                    linesCount = 0
                    collectedData = ""
                    delay(200)
                }
            }
        }
    }

    fun startTranslation() {
        // Reset state
        _translationState.value = null
        isTranslationPaused = false
    }

    fun stopTranslation() {
        isTranslationPaused = true
    }

    @Volatile
    private var _isTranslationInProgress = false

    private fun translate(data: String) {
        if (_isTranslationInProgress) {
            return
        } else {
            _isTranslationInProgress = true
        }

        // Update state
        if (_translationState.value !is TranslationState.Translated) {
            _translationState.postValue(TranslationState.Translating)
        }

        _netRepo.translate(data, object : NetRepository.Callback<String> {
            override fun onData(data: String?) {
                // Update flag
                _isTranslationInProgress = false

                val result = if (data == "NULL" || data == null) {
                    "Couldn't translate"
                } else {
                    data
                }

                // Update state
                _translationState.value?.let {
                    if (it is TranslationState.Translated && it.result == result) {
                        // No change
                        return
                    }
                }

                _translationState.postValue(TranslationState.Translated(result))
            }
        })
    }

    sealed class TranslationState {
        object Translating : TranslationState()
        data class Translated(val result: String) : TranslationState()
    }
}