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
import com.scorpion.screenrecorder.activity.SRC_VideoViewActivity
import java.io.File
import java.text.DecimalFormat

class SRC_ScreenRecordingAdapter(list: MutableList<File>) :
    RecyclerView.Adapter<SRC_ScreenRecordingAdapter.MyViewHolder>() {

    var myList = list
    lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.src_screenshot_adapter_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(mContext).load(myList[position].absolutePath).into(holder.image)

//        Helper.setSize(holder.image, 310, 234, true)
//        Helper.setMargin(holder.image, 50, 0, 0, 0)

        SRC_Helper.setSize(holder.imgBorder,320,432,true)
        SRC_Helper.setSize(holder.image,318,430,true)

        holder.image.setOnClickListener {
            mContext.startActivity(
                Intent(mContext, SRC_VideoViewActivity::class.java).putExtra(
                    "path",
                    myList.get(position).absolutePath
                )
            )
        }
//        holder.lay_Item.setOnClickListener {
//            mContext.startActivity(
//                Intent(mContext, VideoViewActivity::class.java).putExtra(
//                    "path",
//                    myList.get(position).absolutePath
//                )
//            )
//        }

//        try {
//
//            val metaRetriever = MediaMetadataRetriever()
//            metaRetriever.setDataSource(myList.get(position).absolutePath)
//            val height: String =
//                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
//            val width: String =
//                metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!
//            Log.d("HeightWidth", "onBindViewHolder: " + width + "x" + height)
//
//            holder.txtFileResolution.setText(width + "*" + height)
//
//        } catch (e: Exception) {
//
//        }

//        getFileSize(myList.get(position))
//        holder.txtFileSize.setText(getFileSize(myList.get(position)))
//        val strFileName: String = myList.get(position).getName()
//        holder.txtFileName.setText(strFileName)

    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
        val imgBorder = itemView.findViewById<ConstraintLayout>(R.id.imgBorder)

//        val txtFileName = itemView.findViewById<TextView>(R.id.txtFileName)
//        val txtFileResolution = itemView.findViewById<TextView>(R.id.txtFileResolution)
//        val txtFileSize = itemView.findViewById<TextView>(R.id.txtFileSize)
//        val lay_Item = itemView.findViewById<ConstraintLayout>(R.id.lay_Item)

    }

    private val format: DecimalFormat = DecimalFormat("#.##")
    private val MiB = 1024 * 1024.toLong()
    private val KiB: Long = 1024

    fun getFileSize(file: File): String? {
        require(file.isFile) { "Expected a file" }
        val length = file.length().toDouble()
        if (length > MiB) {
            return format.format(length / MiB).toString() + " MB"
        }
        return if (length > KiB) {
            format.format(length / KiB).toString() + " KB"
        } else format.format(length).toString() + " B"
    }
}