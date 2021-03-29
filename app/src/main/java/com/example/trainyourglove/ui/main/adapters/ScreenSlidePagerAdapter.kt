package com.example.trainyourglove.ui.main.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.trainyourglove.R
import com.example.trainyourglove.ui.main.fragments.record.RecordGestureFragment
import com.example.trainyourglove.ui.main.fragments.sync.SyncFragment
import com.example.trainyourglove.ui.main.fragments.synced.SyncedFragment

class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    val title = arrayOf(
        R.string.main_tab_1,
        R.string.main_tab_2,
        R.string.main_tab_3
    )

    private val fragments = arrayOf(
        RecordGestureFragment(),
        SyncFragment(),
        SyncedFragment()
    )

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    override fun getItemCount(): Int {
        return fragments.size
    }
}