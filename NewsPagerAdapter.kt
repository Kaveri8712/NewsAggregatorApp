package com.example.newsaggregatorapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class NewsPagerAdapter(
    activity: FragmentActivity,
    private val categories: List<String>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return CategoryFragment.newInstance(categories[position])
    }
} 