package com.scorpion.screenrecorder.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.scorpion.screenrecorder.fragment.SRC_ScreenShotFragment
import com.scorpion.screenrecorder.fragment.SRC_SettingsFragment
import com.scorpion.screenrecorder.fragment.SRC_VideosFragment

class SRC_TabsAdapter(var mContext: Context, fm: FragmentManager/*, var totalCount: Int*/) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SRC_VideosFragment()
            1 -> SRC_ScreenShotFragment()
            else -> SRC_SettingsFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }
}