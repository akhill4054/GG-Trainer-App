package com.example.trainyourglove.adapters

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.example.trainyourglove.R
import com.example.trainyourglove.databinding.ItemPairedDeviceInfoBinding

class PairedDevicesAdapter(
    context: Context,
    devices: List<BluetoothDevice>
) : ArrayAdapter<BluetoothDevice>(context, 0, devices) {
    private lateinit var itemBinding: ItemPairedDeviceInfoBinding

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = getItem(position)

        if (convertView == null) {
            itemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.item_paired_device_info,
                parent,
                false
            )
        }

        itemBinding.device = device

        return itemBinding.root
    }
}