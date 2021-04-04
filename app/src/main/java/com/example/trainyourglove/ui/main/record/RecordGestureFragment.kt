package com.example.trainyourglove.ui.main.record

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.example.trainyourglove.MAX_ACC_VALUE
import com.example.trainyourglove.MIN_ACC_VALUE
import com.example.trainyourglove.MIN_RECORDING_TIME_IN_SECONDS
import com.example.trainyourglove.R
import com.example.trainyourglove.connectivity.AppBluetooth
import com.example.trainyourglove.databinding.FragmentRecordGestureBinding
import com.example.trainyourglove.ui.main.widgets.Visualizer
import com.example.trainyourglove.utils.AppLogger
import com.example.trainyourglove.utils.SnackBarInterface
import kotlinx.coroutines.flow.collect

class RecordGestureFragment : Fragment() {
    private lateinit var _binding: FragmentRecordGestureBinding

    private lateinit var mVisualizer: Visualizer

    private val _viewModel: RecordingViewModel by viewModels()

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_record_gesture, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializing graph view
        mVisualizer = _binding.graph

        setupViews()
        subscribeObservers()
    }

    private fun setupViews() {
        // Mapping editText
        _binding.mappingEditText.addTextChangedListener {
            // Hide error, if visible
            _binding.mappingEditTextLayout.isErrorEnabled = false
        }
        _binding.mappingEditTextLayout.setEndIconOnClickListener {
            // Clear text
            _binding.mappingEditText.setText("")
        }

        // Click listeners
        _binding.save.setOnClickListener {
            val mappingText = _binding.mappingEditText.text.toString()

            if (mappingText.isNotEmpty()) {
                _viewModel.save(mappingText)

                // Clear mapping editText
                _binding.mappingEditText.setText("")

                // Hide keyboard
                hideKeyboard()

                showSnackBar("Saved")
            } else {
                _binding.mappingEditTextLayout.error =
                    "Please enter a meaning for the recorded gesture..."
                _binding.mappingEditTextLayout.isErrorEnabled = true
                showKeyBoard(_binding.mappingEditText)
            }
        }
        _binding.start.setOnClickListener {
            _viewModel.startResumeRecording()
        }
        _binding.pause.setOnClickListener {
            _viewModel.pauseRecording()
        }
        _binding.stopActive.setOnClickListener {
            if (_viewModel.isRecordingValidForSave()) {
                _viewModel.stop()
            } else {
                // Pause the recording
                _viewModel.pauseRecording()
                // Show message
                showSnackBar("Recording should be at least $MIN_RECORDING_TIME_IN_SECONDS seconds long.")
            }
        }
        _binding.cancel.setOnClickListener {
            _viewModel.discardRecording()

            // Hide keyboard
            hideKeyboard()
        }

        // Debug
        _binding.start.setOnLongClickListener {
            AppBluetooth.getInstance().turnOnFakeMode(requireActivity())
            true
        }
        _binding.stop.setOnLongClickListener {
            AppBluetooth.getInstance().turnOffFakeMode()
            true
        }
    }

    private fun showKeyBoard(editText: EditText) {
        editText.requestFocus()
        editText.postDelayed({
            val keyboard =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            keyboard!!.showSoftInput(editText, 0)
        }, 200)
    }

    private fun subscribeObservers() {
        _viewModel.appBluetooth.readData.asLiveData().observe(viewLifecycleOwner, { data ->
            AppLogger.getInstance(requireActivity().application).log(data)
        })

        _viewModel.appBluetooth.onVisualizerData.observe(viewLifecycleOwner, { floats ->
            mVisualizer.updateValues(floats)
        })

        _viewModel.formattedElapsedRecordingTime.asLiveData()
            .observe(viewLifecycleOwner, { time ->
                _binding.elapsedTime.text = time
            })

        lifecycleScope.launchWhenStarted {
            _viewModel.recordingState.collect { state ->
                _binding.recordingState = state

                when (state) {
                    is RecordingViewModel.RecordingState.Recorded -> {
                        // Show keyboard
                        showKeyBoard(_binding.mappingEditText)
                    }
                    is RecordingViewModel.RecordingState.Error -> {
                        if (state.msg.isNotEmpty()) {
                            showSnackBar(state.msg)
                        }
                    }
                    else -> { // Ideal
                    }
                }
            }
        }
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun showSnackBar(msg: String) {
        (requireActivity() as SnackBarInterface).showSnackBar(msg)
    }

    override fun onResume() {
        super.onResume()

        // Starting the visualization
        mVisualizer.start(MIN_ACC_VALUE, MAX_ACC_VALUE)
    }

//    private var mFakeValuesGeneratorJob: Job? = null
//
//    private fun startFakeGenerator() {
//        mFakeValuesGeneratorJob?.cancel()
//
//        val min = -19000F
//        val max = 19000F
//        val vel = (max - min) * 0.1F // 30%
//
//        val values = FloatArray(6) { 0F }
//
//        mFakeValuesGeneratorJob = lifecycleScope.launch {
//            while (isActive) {
//                for (i in values.indices) {
//                    val dy = if ((0..1).random() == 0) 1 else -1
//                    values[i] += dy * vel
//                    if (dy > 0) {
//                        if (values[i] > max) values[i] = max
//                    } else {
//                        if (values[i] < min) values[i] = min
//                    }
//                }
//                delay(100)
//            }
//        }
//
//        // Provide to visualizer
//        mVisualizer.updateValues(values)
//    }
//
//    private fun stopFakeValueGenerator() {
//        mFakeValuesGeneratorJob?.cancel()
//    }

    override fun onStop() {
        super.onStop()

        // Pause the visualization
        mVisualizer.pause()
    }
}