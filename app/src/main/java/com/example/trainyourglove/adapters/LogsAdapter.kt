package com.example.trainyourglove.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.trainyourglove.R
import com.example.trainyourglove.databinding.ItemLogBinding

class LogsAdapter : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {

    private val logs = mutableListOf<String>()

    fun log(msg: String) {
        logs.add(msg)
        notifyItemInserted(logs.size - 1)
    }

    override fun getItemCount(): Int = logs.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind("${position + 1}. ${logs[position]}")
    }

    class LogViewHolder(
        private val binding: ItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(log: String) {
            binding.log = log
        }

        companion object {
            fun from(parent: ViewGroup): LogViewHolder {
                val binding = DataBindingUtil.inflate<ItemLogBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.item_log,
                    parent,
                    false
                )
                return LogViewHolder(binding)
            }
        }
    }
}