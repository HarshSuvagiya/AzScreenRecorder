package com.scorpion.screenrecorder.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.scorpion.screenrecorder.R
import java.io.File

class SRC_ImageViewPagerAdapter(list: MutableList<File>, context: Context) : PagerAdapter() {

    val myList = list
    val mContext = context

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val view = LayoutInflater.from(mContext).inflate(R.layout.src_image_view_for_view_pager,null)
        val imageView =view.findViewById<ImageView>(R.id.image)
        Glide.with(mContext).load(myList.get(position).absolutePath).into(imageView)

        val viewPager = container as ViewPager
        viewPager.addView(view,0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager = container as ViewPager
        val view = `object` as View
        viewPager.removeView(view)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return myList.size
    }
}