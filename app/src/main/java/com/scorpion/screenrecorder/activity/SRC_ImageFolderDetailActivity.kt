package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.adapter.SRC_ImageFolderDetailAdapter
import com.scorpion.screenrecorder.adapter.SRC_SelectedImageAdapter
import com.scorpion.screenrecorder.model.SRC_ImageFolderDetail
import com.scorpion.screenrecorder.utils.SRC_Utils
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.*
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.back
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.bottomLayout
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.done
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.selectedImageCount
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.selectedImageRecyclerView
import kotlinx.android.synthetic.main.src_activity_image_folder_detail.topBar
import com.scorpion.screenrecorder.SRC_Helper

class SRC_ImageFolderDetailActivity : AppCompatActivity() {

    lateinit var imageDataDetail: SRC_ImageFolderDetail
    lateinit var selectedImageAdapter: SRC_SelectedImageAdapter
    lateinit var mActivity : Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_image_folder_detail)

        mActivity = this

        SRC_Helper.FS(mActivity)

        setSize()

        back.setOnClickListener {
            finish()
        }
        done.setOnClickListener {
            finish()
        }
        bucketNameTV.isSelected = true

        selectedImageAdapter = SRC_SelectedImageAdapter(SRC_Utils.selectedImageList, object : SRC_SelectedImageAdapter.OnImageClick {
            override fun getImageClick(position: Int) {
                selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
            }
        })
        selectedImageRecyclerView.layoutManager = LinearLayoutManager(this@SRC_ImageFolderDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        selectedImageRecyclerView.adapter = selectedImageAdapter
        selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
        imageDataDetail = intent.getSerializableExtra("imageDataDetail") as SRC_ImageFolderDetail
        bucketNameTV.text = intent.getStringExtra("folderName")
        folderDetailRecyclerView.apply {
            adapter = SRC_ImageFolderDetailAdapter(imageDataDetail.path, object : SRC_ImageFolderDetailAdapter.OnImageClick {
                override fun getImageClick(path: String) {
                    addImage()
                }
            })
            layoutManager = GridLayoutManager(this@SRC_ImageFolderDetailActivity, 3)
        }
    }

    fun setSize(){
        SRC_Helper.setSize(bottomLayout,1080,350,true)
        SRC_Helper.setSize(topBar,1080,152,true)
        SRC_Helper.setSize(back,60,60,true)
        SRC_Helper.setSize(done,162,160,true)

        SRC_Helper.setMargin(back,50,0,0,0)
        SRC_Helper.setMargin(done,0,0,50,250)
    }

    override fun onRestart() {
        super.onRestart()
        selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
    }

    fun addImage() {
        selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
        selectedImageRecyclerView.scrollToPosition(SRC_Utils.selectedImageList.size - 1)
        selectedImageAdapter.notifyDataSetChanged()
    }

    fun Back(view: View) {
        onBackPressed()
    }
}