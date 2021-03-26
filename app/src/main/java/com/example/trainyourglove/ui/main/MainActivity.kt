package com.example.trainyourglove.ui.main

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.example.trainyourglove.R
import com.example.trainyourglove.connectivity.AppBluetooth
import com.example.trainyourglove.databinding.ActivityMainBinding
import com.example.trainyourglove.ui.main.adapters.ScreenSlidePagerAdapter
import com.example.trainyourglove.utils.AppLogger
import com.example.trainyourglove.utils.showShortToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter

    private val viewModel: MainViewModel by viewModels()

    private val appBluetooth: AppBluetooth by lazy { AppBluetooth.getInstance() }

//    private lateinit var logsAdapter: LogsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        AppLogger.getInstance(application).log("App started") // DEBUG

        // Setting up viewpager
        pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager = binding.viewPager
        viewPager.adapter = pagerAdapter

        // Setting up tab layout
        TabLayoutMediator(binding.tabs, viewPager) { tab, position ->
            tab.setText(pagerAdapter.title[position])
        }.attach()

        // Click listeners
        binding.connect.setOnClickListener(this)
        binding.connect.setOnLongClickListener {
            AppLogger.getInstance(application).shareActiveLog(this)
            true
        }

        // Logs list
//        logsAdapter = LogsAdapter()
//        binding.debugLogs.adapter = logsAdapter

        subscribeObservers()

        // Try to connect with the last connection.
        appBluetooth.tryToConnectWithTheLastConnection(this)
    }

    private fun subscribeObservers() {
        appBluetooth.connectionState.observe(this, { state ->
            setupConnectButtonState(state!!)
        })
    }

    private var mConnectingAnimator: ValueAnimator? = null

    private fun setupConnectButtonState(state: AppBluetooth.ConnectionState) {
        // Remove animator on state change
        mConnectingAnimator?.cancel()
        mConnectingAnimator = null

        when (state) {
            AppBluetooth.ConnectionState.Connected -> {
                binding.connect.setColorFilter(ContextCompat.getColor(this, R.color.lightGreen))
                binding.connect.alpha = 1F
            }
            AppBluetooth.ConnectionState.Error -> {
                showShortToast("Couldn't connect")

                binding.connect.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                binding.connect.alpha = 0.5F
            }
            AppBluetooth.ConnectionState.Connecting -> {
                binding.connect.alpha = 0.8F

                mConnectingAnimator = ValueAnimator.ofArgb(
                    ContextCompat.getColor(this, R.color.colorPrimary),
                    ContextCompat.getColor(this, R.color.lightGreen),
                    ContextCompat.getColor(this, R.color.colorPrimary)
                ).apply {
                    // Add animation update listener
                    addUpdateListener { animation ->
                        binding.connect.setColorFilter(animation.animatedValue as Int)
                        repeatCount = Animation.INFINITE
                        duration = 1000
                    }
                    start() // STart animation
                }
            }
            AppBluetooth.ConnectionState.Disconnected -> { // Disconnected
                showShortToast("Disconnected")

                binding.connect.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                binding.connect.alpha = 0.5F
            } // else; IDEAL
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restoring current page
        binding.viewPager.currentItem = viewModel.getIndex()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Saving current page
        viewModel.setIndex(binding.viewPager.currentItem)
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem -= 1
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        appBluetooth.onEnableBluetoothActivityResult(this, requestCode, resultCode)
    }

    override fun onDestroy() {
        super.onDestroy()

        appBluetooth.close() // If open/connected
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.connect -> {
                connect()
            }
        }
    }

    private fun connect() {
        if (appBluetooth.isConnected) {
            // Show option to disconnect
            MaterialAlertDialogBuilder(this)
                .setTitle("Already connected")
                .setMessage("You may disconnect if you want.")
                .setNegativeButton("Disconnect") { dialog, _ ->
                    appBluetooth.close()
                    dialog.dismiss()
                }.setPositiveButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        } else {
            appBluetooth.connect(this)
        }
    }
}