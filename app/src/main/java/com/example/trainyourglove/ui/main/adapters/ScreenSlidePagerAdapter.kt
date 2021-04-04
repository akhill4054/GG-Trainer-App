package com.example.trainyourglove.ui.main.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.trainyourglove.ui.main.record.RecordGestureFragment
import com.example.trainyourglove.ui.main.recorded.RecordedFragment
import com.example.trainyourglove.ui.main.translate.TranslateFragment

class ScreenSlidePagerAdapter(
    fm: FragmentManager
) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val titles = arrayOf(
        "Translate",
        "Record",
        "Recorded"
    )

    private val fragments = arrayOf(
        TranslateFragment(),
        RecordGestureFragment(),
        RecordedFragment()
    )

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }
}