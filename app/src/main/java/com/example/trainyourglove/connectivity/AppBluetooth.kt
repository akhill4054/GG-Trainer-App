package com.example.trainyourglove.connectivity

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.trainyourglove.END_OF_LINE_CHAR
import com.example.trainyourglove.R
import com.example.trainyourglove.RC__ENABLE_BLUETOOTH
import com.example.trainyourglove.START_OF_LINE_CHAR
import com.example.trainyourglove.adapters.PairedDevicesAdapter
import com.example.trainyourglove.data.PreferenceManager
import com.example.trainyourglove.databinding.DialogPairedDevicesBinding
import com.example.trainyourglove.utils.showShortToast
import com.example.trainyourglove.utils.vibrateDevice
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.util.*

private const val TAG = "AppBluetooth"

class AppBluetooth private constructor() {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    val connectionState = _connectionState.asStateFlow()

    val isConnected: Boolean
        get() {
            return connectionState.value is ConnectionState.Connected
        }

    val isDisconnected: Boolean
        get() {
            return !isConnected
        }

    private val _readData = MutableSharedFlow<String>()

    val readData: SharedFlow<String> = _readData

    private val _onVisualizerData by lazy { MutableLiveData<FloatArray>() }

    val onVisualizerData: LiveData<FloatArray> by lazy { _onVisualizerData }

    private val _onTranslationData by lazy { MutableLiveData<String>() }

    val onTranslationData: LiveData<String> by lazy { _onTranslationData }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // Hardcoded UUID
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val mScope = CoroutineScope(Dispatchers.IO)

    private var mmSocket: BluetoothSocket? = null

    private var mConnection: BluetoothConnection? = null

    fun connect(activity: Activity): AppBluetooth {
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            activity.showShortToast("Device doesn't support Bluetooth")
            _connectionState.value = ConnectionState.Error()
            return this
        }

        // If enabling Bluetooth succeeds, your activity receives the RESULT_OK
        // result code in the onActivityResult() callback. If Bluetooth was not enabled
        // due to an error (or the user responded "No") then the result code is RESULT_CANCELED.
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, RC__ENABLE_BLUETOOTH)
        } else {
            displayPairedDevices(activity)
        }

        // Optionally, your application can also listen for the ACTION_STATE_CHANGED broadcast intent,
        // which the system broadcasts whenever the Bluetooth state changes. This broadcast contains the extra
        // fields EXTRA_STATE and EXTRA_PREVIOUS_STATE, containing the new and old Bluetooth states, respectively.
        // Possible values for these extra fields are STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF, and STATE_OFF.
        // Listening for this broadcast can be useful if your app needs to detect runtime changes made to the Bluetooth state.
        return this
    }

    fun onEnableBluetoothActivityResult(context: Context, requestCode: Int, resultCode: Int) {
        if (requestCode == RC__ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                displayPairedDevices(context)
            } else {
                context.showShortToast("Permission denied")
            }
        }
    }

    private fun displayPairedDevices(context: Context) {
        // Dialog view
        val binding = DataBindingUtil.inflate<DialogPairedDevicesBinding>(
            LayoutInflater.from(context),
            R.layout.dialog_paired_devices,
            null,
            false
        )

        val pairedDevices: List<BluetoothDevice>? = bluetoothAdapter?.bondedDevices?.toList()

        if (!pairedDevices.isNullOrEmpty()) {
            // Setup list view
            val devicesListAdapter = PairedDevicesAdapter(context, pairedDevices)
            binding.pairedDevices.adapter = devicesListAdapter

            val dialog = MaterialAlertDialogBuilder(context)
                .setView(binding.root)
                .setPositiveButton("Pair a new device") { _, _ ->
                    pairNewDevice(context)
                }.setNegativeButton(
                    "Cancel"
                ) { _, _ -> }.show()

            // Item click listener
            binding.pairedDevices.setOnItemClickListener { _, _, position, _ ->
                val selectedDevice = pairedDevices[position]
                initConnectionAsClient(context.applicationContext, selectedDevice)

                // Dismiss dialog
                dialog.dismiss()
            }
        } else {
            context.showShortToast("Pair with the device")
            pairNewDevice(context)
        }
    }

    private fun pairNewDevice(context: Context) {
        context.startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    fun tryToConnectWithTheLastConnection(context: Context) {
        val address = PreferenceManager.getInstance(context).getLastConnectionAddress()
        if (address.isNotEmpty()) {
            val device = bluetoothAdapter!!.getRemoteDevice(address)
            initConnectionAsClient(context, device, true)
        }
    }

    private fun initConnectionAsClient(
        context: Context,
        device: BluetoothDevice,
        suppressToasts: Boolean = false
    ) {
        // Change state
        _connectionState.value = ConnectionState.Connecting

        mScope.launch(Dispatchers.IO) {
            try {
                this@AppBluetooth.mmSocket = device.createRfcommSocketToServiceRecord(uuid)

                // Cancel discovery because it otherwise slows down the connection.
                bluetoothAdapter!!.cancelDiscovery()

                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket!!.connect()

                withContext(Dispatchers.Main) {
                    _connectionState.value = ConnectionState.Connected

                    // For indication purpose
                    context.vibrateDevice(100)

                    // Save connection in prefs
                    PreferenceManager.getInstance(context).setLastConnectionAddress(device)

                    // The connection attempt succeeded. Perform work associated with
                    // the connection in a separate thread.
                    // Starting the connection
                    mConnection = BluetoothConnection(mmSocket!!)
                }
            } catch (e: IOException) {
                Log.e(TAG, "initConnectionAsClient: ", e)

                _connectionState.value = ConnectionState.Error(suppressToasts)
            }
        }
    }

    private inner class BluetoothConnection(private val mmSocket: BluetoothSocket) {
        private val inStreamJob: Job

        private val mmInStream = mmSocket.inputStream
        private val mmOutStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(512)

        init {
            inStreamJob = mScope.launch(Dispatchers.IO) {

                var bytes: Int
                var readMessage: String

                var prevBufferString = ""

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    // Read from the InputStream.
                    try {
                        bytes = mmInStream.read(mmBuffer)

                        String(mmBuffer, 0, bytes).let { bufferString ->
                            // Reset read message
                            readMessage = ""

                            if (prevBufferString.isNotEmpty()) {
                                val obtainedString = prevBufferString + bufferString

                                // Extract complete lines from obtainedString
                                var doesLineHasStart = false
                                var doesLineHasEnd = false
                                var line = ""

                                for (c in obtainedString) {
                                    line += c

                                    if (c == START_OF_LINE_CHAR) {
                                        doesLineHasStart = true
                                    } else if (c == END_OF_LINE_CHAR) {
                                        doesLineHasEnd = true
                                    }

                                    if (doesLineHasStart && doesLineHasEnd) {
                                        // Remove start and end of the line characters and append with line break.
                                        line = line.slice(1 until (line.length - 1)) + '\n'
                                        readMessage += line
                                        // Reset line
                                        line = ""
                                        // Reset flags
                                        doesLineHasStart = false
                                        doesLineHasEnd = false
                                    }
                                }

                                // Store left string in prev buffer string
                                prevBufferString = line
                            } else {
                                // Extract string after first start
                                for (i in bufferString.indices) {
                                    if (bufferString[i] == START_OF_LINE_CHAR) {
                                        // Store string in prev buffer string to be used later
                                        prevBufferString =
                                            bufferString.slice(i until bufferString.length)
                                        break
                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        Log.d(TAG, "Input stream was disconnected", e)

                        close() // Close connection
                        cancel() // Cancel the job

                        break
                    }

                    // Send the obtained bytes to the UI
                    if (readMessage.isNotEmpty()) {
                        _readData.emit(readMessage)
                        Log.d(TAG, readMessage)
                        _onVisualizerData.postValue(
                            formatReadValuesForVisualizer(readMessage)
                        )
                    }
                }
            }
        }

        private fun formatReadValuesForVisualizer(readMessage: String): FloatArray {
            try {
                var firstLine = ""
                for (c in readMessage) {
                    if (c == '\n') break
                    else firstLine += c
                }

                val spiltLine = firstLine.trimEnd().removeSuffix(",").split(",")

                val floats: List<Float> = spiltLine.map { c -> c.toFloat() }

                val floatArray = FloatArray(floats.size)

                // Copy list to array
                for (i in floatArray.indices) {
                    floatArray[i] = floats[i]
                }

                return floatArray
            } catch (e: Exception) {
                Log.e(TAG, "formatReadValuesForVisualizer: ", e)
                return FloatArray(1) { 0F }
            }
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
                // Send a failure message back to the activity.
                return
            }
        }

        fun close() {
            // Cancel job
            inStreamJob.cancel()

            // Close socket io streams
            mmInStream.close()
            mmOutStream.close()

            // Close socket
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "cancel: ")
            }

            // Update state
            _connectionState.value = ConnectionState.Disconnected
            // To prevent toast on cofig. change/view-controller recreation
            _connectionState.value = ConnectionState.Ideal
        }
    }

    fun close() {
        mmSocket = null
        mConnection?.close()

        _connectionState.value = ConnectionState.Disconnected
        // To prevent toast on cofig. change/view-controller recreation
        _connectionState.value = ConnectionState.Ideal
    }

    sealed class ConnectionState {
        object Ideal : ConnectionState()

        object Connected : ConnectionState()

        object Connecting : ConnectionState()

        class Error(val suppressToast: Boolean = false) : ConnectionState()

        object Disconnected : ConnectionState()
    }

    companion object {
        @Volatile
        private var INSTANCE: AppBluetooth? = null

        fun getInstance(): AppBluetooth {
            var localRef = INSTANCE

            return localRef ?: synchronized(AppBluetooth::class.java) {
                localRef = INSTANCE
                localRef ?: AppBluetooth().also {
                    INSTANCE = it
                }
            }
        }
    }
}