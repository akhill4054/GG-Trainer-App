package com.example.trainyourglove.ui.main.fragments.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.trainyourglove.MAX_ACC_VALUE
import com.example.trainyourglove.MIN_ACC_VALUE
import com.example.trainyourglove.R
import com.example.trainyourglove.connectivity.AppBluetooth
import com.example.trainyourglove.databinding.FragmentRecordGestureBinding
import com.example.trainyourglove.ui.main.widgets.Visualizer
import com.example.trainyourglove.utils.AppLogger

class RecordGestureFragment : Fragment() {
    private lateinit var binding: FragmentRecordGestureBinding

    lateinit var visualizer: Visualizer

    private val mAppBluetooth = AppBluetooth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_record_gesture, container, false)
        return binding.root
    }

//    private var mFakeGeneratorJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializing graph view
        visualizer = binding.graph

        subscribeObservers()

//        binding.startPause.setOnClickListener {
//            visualizer.resume()
//        }
//        binding.startPause.setOnLongClickListener {
//            startFakeGenerator()
//            true
//        }
//
//        binding.stop.setOnClickListener {
//            mFakeGeneratorJob?.cancel()
//        }
//
//        // Test
//        startFakeGenerator()
    }

    private fun subscribeObservers() {
        mAppBluetooth.onReadData.observe(viewLifecycleOwner, { data ->
            AppLogger.getInstance(requireActivity().application).log("$$data$")
        })
        mAppBluetooth.onVisualizerData.observe(viewLifecycleOwner, { floats ->
            visualizer.updateValues(floats)
        })
    }

//    private fun startFakeGenerator() {
//        lifecycleScope.launch {
//            mFakeGeneratorJob?.cancelAndJoin()
//            mFakeGeneratorJob = lifecycleScope.launch(Dispatchers.Default) {
//                val n = 6
//                val values = FloatArray(n) { 0F }
//                val incValue = 0.69F
//
//                while (true) {
//                    for (i in 0 until n) {
////                    values[i] = (0..10).random().toFloat()
//                        if ((0..1).random() == 1) {
//                            // ++
//                            values[i] += incValue
//                            if (values[i] > 10F) values[i] = 10F
//                        } else {
//                            // --
//                            values[i] -= incValue
//                            if (values[i] < 0F) values[i] = 0F
//                        }
//                    }
//
//                    withContext(Dispatchers.Main) {
//                        visualizer.updateValues(values)
//                    }
//                    delay(100)
//                }
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()

        // Starting the visualization
        visualizer.start(MIN_ACC_VALUE, MAX_ACC_VALUE)
    }

    override fun onStop() {
        super.onStop()

        // Pause the visualization
        visualizer.pause()
    }
}