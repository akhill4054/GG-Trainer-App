package com.example.trainyourglove.ui.main.fragments.synced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trainyourglove.R
import com.example.trainyourglove.databinding.FragmentSyncedBinding


class SyncedFragment : Fragment() {

    private lateinit var _binding: FragmentSyncedBinding

    private val viewModel: SyncedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_synced,
            container,
            false
        )
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Subscribe observers
        viewModel.syncedGestures.observe(viewLifecycleOwner, { gestures ->
            gestures!!.let {
                _binding.gesturesData = gestures
            }
        })
    }
}