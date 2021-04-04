package com.example.trainyourglove.ui.main.recorded.synced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trainyourglove.R
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.databinding.FragmentSyncedBinding
import com.example.trainyourglove.ui.main.adapters.RecordedGesturesAdapter
import com.example.trainyourglove.utils.SnackBarInterface


class SyncedFragment : Fragment(), RecordedGesturesAdapter.ItemCallback {

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

        // Setup views
        val adapter = RecordedGesturesAdapter(this)
        _binding.gestures.adapter = adapter

        // Subscribe observers
        viewModel.syncedGestures.observe(viewLifecycleOwner, { gestures ->
            gestures!!.let {
                _binding.gesturesData = gestures
                adapter.submitList(gestures)
            }
        })
    }

    override fun onSyncAction(gesture: Gesture) {
        viewModel.unsync(gesture).observe(viewLifecycleOwner, { status ->
            if (status == true) {
                (requireActivity() as SnackBarInterface).showSnackBar("Gesture removed")
            }
        })
    }

    override fun onDelete(gesture: Gesture) {
    }
}