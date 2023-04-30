package com.scorpion.screenrecorder.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scorpion.screenrecorder.R
import java.io.InputStream

class SRC_VideoBackgroundAdapter(list : Array<String>, imageClick: GetImageClick) : RecyclerView.Adapter<SRC_VideoBackgroundAdapter.MyViewHolder>() {

    lateinit var mContext : Context
    var myList : Array<String> = list
    var getImageClick : GetImageClick = imageClick
    private var selected = 0
    interface GetImageClick{
        fun imageClick(pos : Int)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewBg = itemView.findViewById<ImageView>(R.id.imageViewBg)
        var selectedBg = itemView.findViewById<ImageView>(R.id.selectedBg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.src_video_bg_adapter_layout,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    public fun setSelected(selected: Int) {
        this.selected = selected
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var inputstream: InputStream = mContext.assets.open("videobg/${myList[position]}")
        var bitmap: Bitmap = BitmapFactory.decodeStream(inputstream)
        Glide.with(mContext).load(bitmap).into(holder.imageViewBg)
        holder.imageViewBg.setOnClickListener {
            getImageClick.imageClick(position)

            selected = position

            notifyDataSetChanged()

        }

        if (selected == position) {
            holder.selectedBg.setVisibility(View.VISIBLE)
        } else {
            holder.selectedBg.setVisibility(View.GONE)
        }

    }

}