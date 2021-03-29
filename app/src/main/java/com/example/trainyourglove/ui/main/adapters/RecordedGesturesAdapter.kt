package com.example.trainyourglove.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trainyourglove.R
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.databinding.ItemRecordedGestureBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RecordedGesturesAdapter(
    private val itemCallback: ItemCallback
) : ListAdapter<Gesture, RecordedGesturesAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, itemCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        val binding: ItemRecordedGestureBinding,
        val itemCallback: ItemCallback
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // Click listeners
            binding.optionsMenu.setOnClickListener {
                showOptionsMenu()
            }
        }

        private fun showOptionsMenu() {
            PopupMenu(binding.root.context, binding.optionsMenu).run {
                inflate(R.menu.recorded_gesture_options)
                setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.delete) {
                        MaterialAlertDialogBuilder(binding.root.context)
                            .setTitle("Delete the recorded gesture?")
                            .setPositiveButton("Delete") { dialog, _ ->
                                dialog.dismiss()
                                itemCallback.onDelete(binding.gesture!!)
                            }.setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }.show()
                    }
                    true
                }
                show()
            }
        }

        fun bind(gesture: Gesture) {
            binding.gesture = gesture
        }

        companion object {
            fun from(
                parent: ViewGroup,
                itemCallback: ItemCallback
            ): ViewHolder {
                val binding = DataBindingUtil.inflate<ItemRecordedGestureBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_recorded_gesture,
                    parent,
                    false
                )
                return ViewHolder(binding, itemCallback)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Gesture>() {
        override fun areItemsTheSame(oldItem: Gesture, newItem: Gesture): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Gesture, newItem: Gesture): Boolean {
            return oldItem == newItem
        }
    }

    interface ItemCallback {
        fun onDelete(gesture: Gesture)
    }
}