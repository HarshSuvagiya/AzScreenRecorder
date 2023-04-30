package com.scorpion.screenrecorder.adapter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
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

class SRC_ImageFolderDetailAdapter(
    list: MutableList<SRC_ImageData>,
    param: OnImageClick
) :
    RecyclerView.Adapter<SRC_ImageFolderDetailAdapter.MyViewHolder>() {

    var myList = list
    lateinit var mContext: Context
    var onImageClick: OnImageClick = param
    var myReceiverIsRegistered: Boolean = false

    interface OnImageClick {
        fun getImageClick(path: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        mContext = parent.context
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.src_image_folder_detail_adapter_layout, parent, false)
        registerReceiver1()
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        SRC_Utils.selectedImageList.find {
            it.imageUrl == myList[position].imageUrl
        }

        SRC_Helper.setSize(holder.imageView, 324, 310, true)
        SRC_Helper.setSize(holder.selected, 324, 310, true)

        Glide.with(mContext).load(myList[position].imageUrl).into(holder.imageView)
        holder.imageView.setOnClickListener {
            if (!SRC_Utils.selectedImageList.any {
                    it.imageUrl == myList[position].imageUrl
                }) {
                SRC_Utils.selectedImageList.add(myList[position])
                onImageClick.getImageClick(myList[position].imageUrl)
                holder.selected.visibility = View.VISIBLE
            }
        }

        if (SRC_Utils.selectedImageList.any {
                it.imageUrl == myList[position].imageUrl
            }) {
            holder.selected.visibility = View.VISIBLE
        } else {
            holder.selected.visibility = View.GONE
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        val selected = itemView.findViewById<ImageView>(R.id.selected)
    }

    val ReceiveUpdateSelected: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            if (p1?.action.equals("com.mycompany.myapp.SOME_MESSAGE3")) {
                Log.d("UpdatedData", "onReceive: avyu")
                notifyDataSetChanged()
            }

        }


    }

    fun registerReceiver1() {
        if (!myReceiverIsRegistered) {
            myReceiverIsRegistered = true
            mContext.registerReceiver(
                ReceiveUpdateSelected,
                IntentFilter("com.mycompany.myapp.SOME_MESSAGE3")
            )
        }
    }
}