package com.example.trainyourglove.ui.main

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.trainyourglove.R
import com.example.trainyourglove.connectivity.AppBluetooth
import com.example.trainyourglove.data.db.AppDatabase
import com.example.trainyourglove.data.db.entities.Gesture
import com.example.trainyourglove.data.repositories.NetRepository
import com.example.trainyourglove.databinding.ActivityMainBinding
import com.example.trainyourglove.ui.main.adapters.ScreenSlidePagerAdapter
import com.example.trainyourglove.utils.AppLogger
import com.example.trainyourglove.utils.SnackBarInterface
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnClickListener, SnackBarInterface {

    private lateinit var _binding: ActivityMainBinding

    private lateinit var _viewPager: ViewPager
    private lateinit var mPagerAdapter: ScreenSlidePagerAdapter

    private val _viewModel: MainViewModel by viewModels()

    private val _appBluetooth: AppBluetooth by lazy { AppBluetooth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        AppLogger.getInstance(application).log("App started") // DEBUG

        // Setting up viewpager
        mPagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        _viewPager = _binding.viewPager
        _viewPager.adapter = mPagerAdapter

        // Setting up tab layout
        _binding.tabs.setupWithViewPager(_viewPager)

        // DEBUG
        _binding.toolbarTitleText.setOnLongClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("CONFIRM SERVER RESET!")
                .setPositiveButton("Reset") { dialog, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        // Remove synced gestures
                        AppDatabase.getInstance(application).gesturesDao()
                            .removeGestures(Gesture.SYNC_STATUS_SYNCED)
                    }
                    // Reset server
                    NetRepository.getInstance(application)
                        .reset(object : NetRepository.Callback<String> {
                            override fun onData(data: String?) {
                                runOnUiThread {
                                    showSnackBar(data ?: "An error occurred!")
                                }
                            }
                        })
                    showSnackBar("Reset initiated")
                    dialog.dismiss()
                }.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
            true
        }

        // Click listeners
        _binding.connect.setOnClickListener(this)
        _binding.connect.setOnLongClickListener {
            AppLogger.getInstance(application).shareActiveLog(this)
            true
        }

        // Night mode configs.
        val isNightMode =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        _binding.isNightMode = isNightMode

        subscribeObservers()

        // Try to connect with the last connection.
        _appBluetooth.tryToConnectWithTheLastConnection(this)
    }

    private fun subscribeObservers() {
        _appBluetooth.connectionState.asLiveData().observe(this, { state ->
            setupConnectButtonState(state!!)
        })
    }

    private var mConnectingAnimator: ValueAnimator? = null

    private fun setupConnectButtonState(state: AppBluetooth.ConnectionState) {
        // Update binding object
        _binding.connectionState = state

        // Remove animator on state change
        mConnectingAnimator?.cancel()
        mConnectingAnimator = null

        when (state) {
            is AppBluetooth.ConnectionState.Connected -> {
                _binding.connect.setColorFilter(ContextCompat.getColor(this, R.color.lightGreen))
                _binding.connect.alpha = 1F
            }
            is AppBluetooth.ConnectionState.Error -> {
                if (!state.suppressToast) {
                    showSnackBar("Couldn't connect")
                }

                _binding.connect.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                _binding.connect.alpha = 0.5F
            }
            is AppBluetooth.ConnectionState.Connecting -> {
                _binding.connect.alpha = 0.8F

                mConnectingAnimator = ValueAnimator.ofArgb(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    ContextCompat.getColor(this, R.color.lightGreen),
                    ContextCompat.getColor(this, R.color.colorPrimary)
                ).apply {
                    // Add animation update listener
                    addUpdateListener { animation ->
                        _binding.connect.setColorFilter(animation.animatedValue as Int)
                        repeatCount = Animation.INFINITE
                        duration = 1000
                    }
                    start() // STart animation
                }
            }
            else -> { // Disconnected or Ideal
                if (state is AppBluetooth.ConnectionState.Disconnected) {
                    showSnackBar("Disconnected")
                }

                _binding.connect.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                _binding.connect.alpha = 0.5F
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restoring current page
        _binding.viewPager.currentItem = _viewModel.getIndex()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saving current page
        _viewModel.setIndex(_binding.viewPager.currentItem)
    }

    override fun onBackPressed() {
        if (_viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            _viewPager.currentItem -= 1
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        _appBluetooth.onEnableBluetoothActivityResult(this, requestCode, resultCode)
    }

    override fun onDestroy() {
        super.onDestroy()

        _appBluetooth.close() // If open/connected
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.connect -> {
                connect()
            }
        }
    }

    private fun connect() {
        if (_appBluetooth.isConnected) {
            // Show option to disconnect
            MaterialAlertDialogBuilder(this)
                .setTitle("Already connected")
                .setMessage("You may disconnect if you want.")
                .setNegativeButton("Disconnect") { dialog, _ ->
                    _appBluetooth.close()
                    dialog.dismiss()
                }.setPositiveButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        } else {
            _appBluetooth.connect(this)
        }
    }

    override fun showSnackBar(msg: String) {
        Snackbar.make(_binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }
}