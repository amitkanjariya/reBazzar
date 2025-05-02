package com.example.rebazaar.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.rebazaar.databinding.FragmentMyAdsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class MyAdsFragment : Fragment() {
    private lateinit var binding: FragmentMyAdsBinding
    private lateinit var mContext: Context
    private lateinit var myTabsViewPagerAdapter: MyTabsViewPagerAdapter

    override fun onAttach(context: Context) {
        this.mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FAV_ADS", "onViewCreate Starting ")
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ads"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorites"))
        Log.d("FAV_ADS", "onViewCreate Starting 1")
        val fragmentManager = childFragmentManager
        myTabsViewPagerAdapter = MyTabsViewPagerAdapter(fragmentManager, lifecycle)
        binding.viewPager.adapter = myTabsViewPagerAdapter
        Log.d("FAV_ADS", "onViewCreate Starting 2")
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d("FAV_ADS", "tab selected : ${tab.position}")
                binding.viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        Log.d("FAV_ADS", "onViewCreate Starting 3")
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d("FAV_ADS", "onViewCreate Starting 5")
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
                Log.d("FAV_ADS", "onViewCreate Starting 6")
            }
        })
        Log.d("FAV_ADS", "onViewCreate Starting 4")

    }

    class MyTabsViewPagerAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifeCycle){
        override fun createFragment(position: Int): Fragment {
            Log.d("FAV_ADS", "onViewCreate Starting 7")
            if(position==0){
                Log.d("FAV_ADS", "onViewCreate Starting 9")

                return MyAdsAdsFragment()
            } else{
                Log.d("FAV_ADS", "onViewCreate Starting 10")

                return MyAdsFavFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}