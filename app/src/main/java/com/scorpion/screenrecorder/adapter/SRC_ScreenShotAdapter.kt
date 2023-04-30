package com.scorpion.screenrecorder.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.activity.SRC_ScreenshotPreviewActivity
import java.io.File

class SRC_ScreenShotAdapter(list: MutableList<File>) :
    RecyclerView.Adapter<SRC_ScreenShotAdapter.MyViewHolder>() {

    var myList = list
    lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.src_screenshot_adapter_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        SRC_Helper.setSize(holder.imgBorder,320,432,true)
        SRC_Helper.setSize(holder.image,318,430,true)

        Glide.with(mContext).load(myList[position].absolutePath).into(holder.image)
        holder.image.setOnClickListener {
            mContext.startActivity(
                Intent(mContext, SRC_ScreenshotPreviewActivity::class.java).putExtra(
                    "position",
                    position
                )
            )
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
        val imgBorder = itemView.findViewById<ConstraintLayout>(R.id.imgBorder)
    }

}