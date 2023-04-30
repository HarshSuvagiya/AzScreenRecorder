package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import kotlinx.android.synthetic.main.src_activity_image_folder.*
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.adapter.SRC_ImageFolderAdapter
import com.scorpion.screenrecorder.adapter.SRC_SelectedImageAdapter
import com.scorpion.screenrecorder.model.SRC_ImageData
import com.scorpion.screenrecorder.model.SRC_ImageFolderDetail
import com.scorpion.screenrecorder.utils.SRC_Utils
import com.scorpion.screenrecorder.SRC_Helper
import java.io.File
import java.util.*


class SRC_ImageFolderActivity : AppCompatActivity() {

    var folderArrayList: ArrayList<SRC_ImageData> = ArrayList<SRC_ImageData>()
    var folderDetailArrayList: ArrayList<SRC_ImageFolderDetail> = ArrayList<SRC_ImageFolderDetail>()
    lateinit var mContext: Context
    lateinit var selectedImageAdapter: SRC_SelectedImageAdapter
    lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_image_folder)

        mActivity = this

        SRC_Helper.FS(mActivity)

        mContext = this

        loadInterstitial()

        setSize()

        back.setOnClickListener {
            finish()
        }
        done.setOnClickListener {
//            startActivity(Intent(applicationContext,StitchImageActivity::class.java))
//            finish()
            if (SRC_Utils.selectedImageList.size > 1) {
                if (interstitialAd?.isLoaded()!!) {
                    interstitialAd?.setAdListener(object : AdListener() {
                        override fun onAdClosed() {
                            startActivity(
                                Intent(
                                    applicationContext,
                                    SRC_StitchImageActivity::class.java
                                )
                            )
                            finish()
                        }
                    })
                    interstitialAd?.show()
                } else {
                    startActivity(Intent(applicationContext, SRC_StitchImageActivity::class.java))
                    finish()
                }

            } else if (SRC_Utils.selectedImageList.size == 0) {
                Toast.makeText(mContext, "Please Select images", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(mContext, "Please Select atleast 2 images", Toast.LENGTH_LONG).show()
            }

        }

        SRC_Utils.selectedImageList.clear()
        selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
        selectedImageAdapter = SRC_SelectedImageAdapter(
            SRC_Utils.selectedImageList,
            object : SRC_SelectedImageAdapter.OnImageClick {
                override fun getImageClick(position: Int) {
                }
            })
        selectedImageRecyclerView.layoutManager =
            LinearLayoutManager(this@SRC_ImageFolderActivity, LinearLayoutManager.HORIZONTAL, false)
        selectedImageRecyclerView.adapter = selectedImageAdapter
    }

    fun setSize() {
        SRC_Helper.setSize(bottomLayout, 1080, 350, true)
        SRC_Helper.setSize(topBar, 1080, 152, true)
        SRC_Helper.setSize(back, 60, 60, true)
        SRC_Helper.setSize(done, 162, 160, true)

        SRC_Helper.setMargin(back, 50, 0, 0, 0)
        SRC_Helper.setMargin(done, 0, 0, 50, 250)
    }

    override fun onResume() {
        super.onResume()
        GetImageFolders().execute()
        selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
        selectedImageAdapter = SRC_SelectedImageAdapter(
            SRC_Utils.selectedImageList,
            object : SRC_SelectedImageAdapter.OnImageClick {
                override fun getImageClick(position: Int) {
                    selectedImageCount.text = "(${SRC_Utils.selectedImageList.size})"
                }
            })
        selectedImageRecyclerView.layoutManager =
            LinearLayoutManager(this@SRC_ImageFolderActivity, LinearLayoutManager.HORIZONTAL, false)
        selectedImageRecyclerView.adapter = selectedImageAdapter
    }


    inner class GetImageFolders : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            folderArrayList.clear()
        }

        override fun doInBackground(vararg params: String?): String? {
            var proj = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )

            val folderCursor: Cursor =
                getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, null, null,
                    MediaStore.Images.Media.DATE_MODIFIED + " DESC"
                )!!

            if (folderCursor.moveToFirst()) {
                do {
                    try {
                        val path: String = folderCursor.getString(
                            folderCursor
                                .getColumnIndex(MediaStore.Images.Media.DATA)
                        )

                        val name = folderCursor
                            .getString(
                                folderCursor
                                    .getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                            )

                        val width: String = folderCursor
                            .getString(
                                folderCursor
                                    .getColumnIndex(MediaStore.Images.Media.WIDTH)
                            )

                        val height: String = folderCursor
                            .getString(
                                folderCursor
                                    .getColumnIndex(MediaStore.Images.Media.HEIGHT)
                            )
                        val bucketName: String = folderCursor
                            .getString(
                                folderCursor
                                    .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                            )

                        val w = width.toInt()
                        val h = height.toInt()

                        var ratio: Int
                        ratio = if (w > h) {
                            w / h
                        } else {
                            h / w
                        }

                        if (ratio <= 3) {
                            if (!name.endsWith(".gif")) {
                                folderArrayList.add(
                                    SRC_ImageData(
                                        name,
                                        path,
                                        bucketName,
                                        width.toInt(),
                                        height.toInt()
                                    )
                                )
                                Log.d("name123", width)
                                Log.d("name123", height)
                            }
                        }
                    } catch (ee: Exception) {
                    }
                } while (folderCursor.moveToNext())
                folderCursor.close()
            }

            folderDetailArrayList.clear()
            var files: ArrayList<SRC_ImageData>
            for (i in folderArrayList.indices) {
                val folder: String = File(folderArrayList.get(i).imageUrl).getParent()
                var b = false
                var pos = 0
                for (k in folderDetailArrayList.indices) {
                    if (folderDetailArrayList.get(k).folder.equals(folder)) {
                        b = true
                        pos = k
                    }
                }
                if (b) {
                    val fs: ArrayList<SRC_ImageData> = folderDetailArrayList.get(pos).path
                    fs.add(folderArrayList.get(i))
                } else {
                    files = ArrayList()
                    files.add(folderArrayList.get(i))
                    folderDetailArrayList.add(SRC_ImageFolderDetail(folder, files))
                }
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            folderRecyclerView.apply {
                adapter = SRC_ImageFolderAdapter(folderDetailArrayList)
                layoutManager = GridLayoutManager(this@SRC_ImageFolderActivity, 2)
            }
        }

    }

    fun Back(view: View) {
        onBackPressed()
    }

    var interstitialAd: InterstitialAd? = null

    // Load Interstitial
    private fun loadInterstitial() {
        interstitialAd = InterstitialAd(this)
        interstitialAd!!.adUnitId = SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd!!.loadAd(ConsentSDK.getAdRequest(this))
    }
}