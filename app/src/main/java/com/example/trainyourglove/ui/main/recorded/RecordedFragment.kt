package com.example.trainyourglove.ui.main.recorded

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.trainyourglove.R
import com.example.trainyourglove.databinding.FragmentRecordedBinding
import com.example.trainyourglove.ui.main.recorded.sync.SyncFragment
import com.example.trainyourglove.ui.main.recorded.synced.SyncedFragment
import com.google.android.material.tabs.TabLayoutMediator

class RecordedFragment : Fragment() {

    private lateinit var _binding: FragmentRecordedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recorded,
            container,
            false
        )
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = object : FragmentStateAdapter(this) {
            val titles = listOf(
                "Sync",
                "Synced"
            )

            private val fragments = listOf<Fragment>(
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

        _binding.viewPager.adapter = pagerAdapter

        // Setting up tab layout
        TabLayoutMediator(_binding.tabLayout, _binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.titles[position]
        }.attach()
    }
}