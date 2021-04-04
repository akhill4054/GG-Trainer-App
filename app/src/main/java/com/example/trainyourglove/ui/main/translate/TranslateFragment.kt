package com.example.trainyourglove.ui.main.translate

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.asLiveData
import com.example.trainyourglove.MAX_ACC_VALUE
import com.example.trainyourglove.MIN_ACC_VALUE
import com.example.trainyourglove.R
import com.example.trainyourglove.databinding.FragmentTranslateBinding
import java.util.*

class TranslateFragment : Fragment() {

    private lateinit var _binding: FragmentTranslateBinding

    private val _viewModel: TranslationViewModel by activityViewModels()

    private lateinit var _tts: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_translate,
            container,
            false
        )
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TTS
        _tts = TextToSpeech(requireActivity().application) { int ->
            if (int != TextToSpeech.ERROR) {
                _tts.language = Locale.ENGLISH
            }
        }

        // Setup views
        // Subscribe observers
        _viewModel.appBluetooth.onVisualizerData.observe(viewLifecycleOwner, { values ->
            _binding.visualizer.updateValues(values)
        })
        _viewModel.translationState.observe(viewLifecycleOwner, { state ->
            state!!.let {
                _binding.translationState = state

                if (state is TranslationViewModel.TranslationState.Translating) {
                    _binding.lottieAnimation.playAnimation()
                } else {
                    _binding.lottieAnimation.pauseAnimation()

                    val result = (state as TranslationViewModel.TranslationState.Translated).result
                    _binding.translation.text = result
                    // Play TTS feedback
                    _tts.speak(result, TextToSpeech.QUEUE_FLUSH, Bundle(), result)
                }
            }
        })
        _viewModel.appBluetooth.connectionState.asLiveData().observe(viewLifecycleOwner, { state ->
            _binding.connectionState = state
        })
    }

    override fun onResume() {
        super.onResume()

        _binding.visualizer.start(MIN_ACC_VALUE, MAX_ACC_VALUE)
        // Resume translation
        _viewModel.startTranslation()
    }

    override fun onStop() {
        super.onStop()

        _binding.visualizer.pause()
        // Pause translation
        _viewModel.stopTranslation()
    }
}