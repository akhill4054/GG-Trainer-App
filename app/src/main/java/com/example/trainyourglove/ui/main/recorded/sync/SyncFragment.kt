package com.example.trainyourglove.ui.main.recorded.sync

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trainyourglove.R
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.databinding.FragmentSyncBinding
import com.example.trainyourglove.ui.main.adapters.RecordedGesturesAdapter
import com.example.trainyourglove.utils.SnackBarInterface

class SyncFragment : Fragment(), RecordedGesturesAdapter.ItemCallback {

    private lateinit var _binding: FragmentSyncBinding

    private val _viewModel: SyncViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sync,
            container,
            false
        )
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup views
        // Gestures' list
        val gesturesAdapter = RecordedGesturesAdapter(this)
        _binding.gestures.adapter = gesturesAdapter

        // Subscribe observers
        _viewModel.syncGestures.observe(viewLifecycleOwner, { gestures ->
            gestures!!.let {
                _binding.gesturesData = gestures
                gesturesAdapter.submitList(gestures)
            }
        })
    }

    override fun onSyncAction(gesture: Gesture) {
        // Show progress
        _binding.isSyncInProgress = true

        _viewModel.sync(gesture).observe(viewLifecycleOwner, { status ->
            when (status) {
                true -> {
                    showSnackBar("Synced")
                }
                false -> {
                    showSnackBar("Couldn't sync")
                }
                else -> { // Error
                    showSnackBar("An error occurred while trying to sync!")
                }
            }

            // Hide progress
            _binding.isSyncInProgress = false
        })
    }

    override fun onDelete(gesture: Gesture) {
        _viewModel.deleteGesture(gesture)
        showSnackBar("Gesture deleted")
    }

    private fun showSnackBar(msg: String) {
        (requireActivity() as SnackBarInterface).showSnackBar(msg)
    }
}