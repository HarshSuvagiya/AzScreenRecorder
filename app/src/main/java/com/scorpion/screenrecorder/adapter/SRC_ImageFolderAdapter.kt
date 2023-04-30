package com.scorpion.screenrecorder.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.activity.SRC_ImageFolderDetailActivity
import com.scorpion.screenrecorder.model.SRC_ImageFolderDetail
import java.io.File

class SRC_ImageFolderAdapter(list: MutableList<SRC_ImageFolderDetail>) :
    RecyclerView.Adapter<SRC_ImageFolderAdapter.MyViewHolder>() {

    var myList = list
    lateinit var mContext: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        val view =
            LayoutInflater.from(mContext).inflate(R.layout.src_image_folder_adapter_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        SRC_Helper.setSize(holder.folderBorder,480,460,true)
        SRC_Helper.setSize(holder.imageView,478,458,true)
        SRC_Helper.setSize(holder.text_Bar,478,110,true)

        Glide.with(mContext).load(myList[position].path[0].imageUrl).into(holder.imageView)
        val fileData = myList.get(position)
        val folderName: String = fileData.folder
        holder.bucketName.setText(File(folderName).name)

        holder.bucketSize.text=myList[position].path.size.toString()

        holder.bucketName.isSelected = true
        holder.imageView.setOnClickListener {
            mContext.startActivity(Intent(mContext, SRC_ImageFolderDetailActivity::class.java).putExtra("imageDataDetail", myList.get(position)).putExtra("folderName", File(folderName).name))
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        val bucketName = itemView.findViewById<TextView>(R.id.bucketName)
        val folderBorder = itemView.findViewById<ConstraintLayout>(R.id.folderBorder)
        val text_Bar = itemView.findViewById<LinearLayout>(R.id.text_Bar)
        val bucketSize = itemView.findViewById<TextView>(R.id.bucketSize)
    }

}