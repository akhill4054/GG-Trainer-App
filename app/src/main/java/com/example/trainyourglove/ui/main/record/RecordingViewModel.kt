package com.example.trainyourglove.ui.main.record

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainyourglove.MIN_RECORDING_TIME_IN_SECONDS
import com.example.trainyourglove.connectivity.AppBluetooth
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.repositories.GesturesRepository
import com.example.trainyourglove.utils.AppFileUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class RecordingViewModel(application: Application) : AndroidViewModel(application) {

    private val _gesturesRepository by lazy { GesturesRepository.getInstance(application) }

    val appBluetooth = AppBluetooth.getInstance()

    private var mRecordTimer: Timer? = null

    // Elapsed time since the recording started in seconds.
    private val _elapsedRecordingTime by lazy { MutableStateFlow(0) }

    fun isRecordingValidForSave(): Boolean =
        _elapsedRecordingTime.value >= MIN_RECORDING_TIME_IN_SECONDS

    val formattedElapsedRecordingTime = _elapsedRecordingTime.transformLatest { value ->
        if (value <= 60) {
            emit("00 : ${value.formatForTime()}")
        } else {
            val mm = value / 60
            val ss = value % 60
            emit("${mm.formatForTime()} : ${ss.formatForTime()}")
        }
    }

    private fun Int.formatForTime(): String {
        return if (this < 10) "0$this" else "$this"
    }

    private val _recordingSate = MutableStateFlow<RecordingState>(RecordingState.Ideal)

    val recordingState = _recordingSate.asStateFlow()

    // Text file to store recording
    private var recordingFile: File? = null

    sealed class RecordingState {
        object Ideal : RecordingState()
        object Recording : RecordingState()
        object Paused : RecordingState()
        object Recorded : RecordingState()
        class Error(val msg: String) : RecordingState()
    }

    private var recordingJob: Job? = null

    init {
        viewModelScope.launch {
            appBluetooth.connectionState.collect {
                if (appBluetooth.isDisconnected && (recordingState.value is RecordingState.Recording
                            || recordingState.value is RecordingState.Paused)
                ) {
                    // Cancel recording
                    recordingJob?.cancel()
                    // Delete file
                    recordingFile!!.delete()
                    recordingFile = null

                    // Update state
                    _recordingSate.value = RecordingState.Error("Disconnected")
                }
            }
        }
        viewModelScope.launch {
            recordingState.collect { state ->
                when (state) {
                    is RecordingState.Recording -> {
                        // Start the timer
                        mRecordTimer = Timer()
                        mRecordTimer!!.scheduleAtFixedRate(object : TimerTask() {
                            override fun run() {
                                // Update elapsed time
                                _elapsedRecordingTime.value = _elapsedRecordingTime.value + 1
                            }
                        }, 1000L, 1000L)
                    }
                    is RecordingState.Paused -> {
                        mRecordTimer?.cancel()
                    }
                    is RecordingState.Recorded -> {
                        mRecordTimer?.cancel()
                    }
                    else -> {
                        // Reset the timer
                        mRecordTimer?.cancel()
                        mRecordTimer = null
                        _elapsedRecordingTime.value = 0
                    }
                }
            }
        }
    }

    fun startResumeRecording() {
        if (!appBluetooth.isConnected) {
            // Update state
            _recordingSate.value = RecordingState.Error("Please connect to start the recording")
            return
        }

        if (recordingFile == null) {
            // Create and open file, if not
            recordingFile = AppFileUtils.createNewRecordFile(getApplication())
        }

        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            // Listen to incoming data
            appBluetooth.readData.collect { readData ->
                // Write to file
                recordingFile!!.appendText(readData)
            }
        }

        // Update state
        _recordingSate.value = RecordingState.Recording
    }

    fun pauseRecording() {
        recordingJob?.cancel()
        // Update state
        _recordingSate.value = RecordingState.Paused
    }

    fun stop() {
        // Cancel recording
        recordingJob?.cancel()
        // Update state
        _recordingSate.value = RecordingState.Recorded
    }

    fun save(mappedString: String) {
        if (mappedString.isEmpty()) {
            throw RuntimeException("Mapped text cannot be empty!")
        }

        // Save file
        viewModelScope.launch {
            _gesturesRepository.insert(
                Gesture(
                    mappedText = mappedString,
                    dataFileUri = recordingFile!!.toURI().toString(),
                    durationMillis = _elapsedRecordingTime.value * 1000L
                )
            )
        }

        recordingFile = null
        // Update state
        _recordingSate.value = RecordingState.Ideal
    }

    fun discardRecording() {
        // Delete file
        recordingFile!!.delete()
        recordingFile = null
        // Update state
        _recordingSate.value = RecordingState.Ideal
    }

    override fun onCleared() {
        super.onCleared()

        // Delete file if still open
        recordingFile?.delete()
    }
}