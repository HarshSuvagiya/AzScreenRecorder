package com.scorpion.screenrecorder.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.model.SRC_ImageData
import com.scorpion.screenrecorder.utils.SRC_Utils

class SRC_SelectedImageAdapter(list: MutableList<SRC_ImageData>, param: SRC_SelectedImageAdapter.OnImageClick) :
    RecyclerView.Adapter<SRC_SelectedImageAdapter.MyViewHolder>() {

    var myList = list
    lateinit var mContext: Context
    var onImageClick: SRC_SelectedImageAdapter.OnImageClick = param

    interface OnImageClick{
        fun getImageClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext).inflate(R.layout.src_selected_image_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        SRC_Helper.setSize(holder.image,178,160,true)
        SRC_Helper.setSize(holder.remove,198,180,true)

        Glide.with(mContext).load(myList[position].imageUrl).into(holder.image)
        val fileData = myList.get(position)
        holder.remove.setOnClickListener {
            SRC_Utils.selectedImageList.removeAt(position)
            onImageClick.getImageClick(position)
            notifyDataSetChanged()

            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE3")
            intent.putExtra("UpdateSelected", "SelectedItem")
            mContext.sendBroadcast(intent)

        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.image)
        val remove = itemView.findViewById<ImageView>(R.id.remove)
    }

}