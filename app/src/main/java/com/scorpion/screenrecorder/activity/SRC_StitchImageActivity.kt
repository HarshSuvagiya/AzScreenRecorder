package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.model.SRC_ImageData
import com.scorpion.screenrecorder.utils.SRC_FileUtils
import com.scorpion.screenrecorder.utils.SRC_Helper.gone
import com.scorpion.screenrecorder.utils.SRC_Helper.visible
import com.scorpion.screenrecorder.utils.SRC_Utils
import kotlinx.android.synthetic.main.src_activity_image_folder.back
import kotlinx.android.synthetic.main.src_activity_stitch_image.*
import kotlinx.android.synthetic.main.src_stitch_vertical_scroll.view.*
import com.scorpion.screenrecorder.utils.SRC_Helper
import java.io.File

class SRC_StitchImageActivity : AppCompatActivity() {

    lateinit var mContext: Context
    var imageList: MutableList<SRC_ImageData> = ArrayList<SRC_ImageData>()
    var height: Int = 0
    var width: Int = 0
    var dY: Float = 0F
    var dY2: Float = 0F
    lateinit var progressDialog: ProgressDialog
    var count: Int = 0
    lateinit var mActivity: Activity
    private var adContainerView: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_stitch_image)

        mContext = this

        mActivity = this

        com.scorpion.screenrecorder.SRC_Helper.FS(mActivity)

        MobileAds.initialize(
            this
        ) { }

        adContainerView = findViewById(R.id.ad_view_container)
// Step 1 - Create an AdView and set the ad unit ID on it.
// Step 1 - Create an AdView and set the ad unit ID on it.
        adView = AdView(this)
        adView?.adUnitId = SRC_Utils.BANNER_ID
        adContainerView?.addView(adView)
        loadBanner()

        setSize()

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Saving...")
        init()
        imageList.addAll(SRC_Utils.selectedImageList)

        setVerticalScrollLayout()
        done.setOnClickListener {
            hideAllViews()
            SaveAsyncTask().execute()
        }
    }

    fun setSize() {
        com.scorpion.screenrecorder.SRC_Helper.setSize(
            topBar,
            1080,
            152,
            true
        )
        com.scorpion.screenrecorder.SRC_Helper.setSize(
            back,
            60,
            60,
            true
        )
        com.scorpion.screenrecorder.SRC_Helper.setSize(
            done,
            99,
            120,
            true
        )

        com.scorpion.screenrecorder.SRC_Helper.setMargin(
            back,
            50,
            0,
            0,
            0
        )
        com.scorpion.screenrecorder.SRC_Helper.setMargin(
            done,
            0,
            0,
            50,
            0
        )
    }

    private fun init() {
        width = resources.displayMetrics.widthPixels
        height = resources.displayMetrics.heightPixels
    }

    private fun setVerticalScrollLayout() {
        for (image in imageList) {
            addImageToVerticalScroll(image)
        }
    }

    private fun addImageToVerticalScroll(image: SRC_ImageData) {
        val myview: View = LayoutInflater.from(applicationContext)
            .inflate(R.layout.src_stitch_vertical_scroll, null, false)

        val img = myview.findViewById<ImageView>(R.id.img)
        val cut = myview.findViewById<ImageView>(R.id.cut)
        val close = myview.findViewById<ImageView>(R.id.close)
        val save = myview.findViewById<ImageView>(R.id.save)
        val reset = myview.findViewById<ImageView>(R.id.reset)
        val bottomLayout = myview.findViewById<View>(R.id.bottomLayout)

        val cut_lay = myview.findViewById<RelativeLayout>(R.id.cut_lay)

        val lay_top = myview.findViewById<LinearLayout>(R.id.lay_top)
        val top_img = myview.findViewById<ImageView>(R.id.top_img)
        val tra_top = myview.findViewById<ImageView>(R.id.tra_top)

        val lay_bot = myview.findViewById<LinearLayout>(R.id.lay_bot)
        val bot_img = myview.findViewById<ImageView>(R.id.bot_img)
        val tra_bot = myview.findViewById<ImageView>(R.id.tra_bot)

        com.scorpion.screenrecorder.SRC_Helper.setSize(cut,104,104,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(close,104,104,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(save,104,104,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(myview.top_img,186,62,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(myview.bot_img,186,62,true)

        cut.setOnClickListener {
            visible(close)
            visible(save)
            visible(cut_lay)
            gone(cut)
        }

        close.setOnClickListener {
            gone(close)
            gone(save)
            gone(cut_lay)
            visible(cut)
//            val height = cut_lay.height.toFloat()
//            lay_bot.y = height - Helper.w(62)
//            val params = RelativeLayout.LayoutParams(width, 0)
//            tra_bot.layoutParams = params
//            lay_top.y = 0f
//            val params1 = RelativeLayout.LayoutParams(width, 0)
//            tra_top.layoutParams = params1
        }

        save.setOnClickListener {
            gone(close)
            gone(save)
            gone(cut_lay)
            visible(cut)
            cropBitmapSC(img, myview, cut_lay, lay_top, lay_bot, tra_top, tra_bot, bot_img)
        }

        top_img.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                dY = 0F
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        verticalScrollView.requestDisallowInterceptTouchEvent(true)
                        dY = lay_top.y - event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val height = cut_lay.height.toFloat()
                        val width = cut_lay.width
                        val f = event.rawY + dY
                        val val1: Float = height / 2 - SRC_Helper.w(62)
                        if (f >= 0 && f < val1) {
                            lay_top.y = f
                            val params = RelativeLayout.LayoutParams(width, f.toInt())
                            tra_top.layoutParams = params
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        verticalScrollView.requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                }

                return false
            }
        })

        bot_img.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                dY2 = 0f
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        verticalScrollView.requestDisallowInterceptTouchEvent(true)
                        dY2 = lay_bot.y - event.rawY - SRC_Helper.h(124)
//                        Log.d("fff", lay_bot.y.toString())
//                        Log.d("fff", event.rawY.toString())
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val height = cut_lay.height.toFloat()
                        val width = cut_lay.width
                        val f = event.rawY + dY2
                        val val1 = height / 2
                        Log.d("fff", bot_img.height.toString())
                        Log.d("fff1", height.toString())
                        Log.d("fff2", f.toString())
                        if (f >= val1 && f < height - bot_img.height) {
                            lay_bot.y = f
                            val params = RelativeLayout.LayoutParams(
                                width,
                                (height - f - bot_img.height).toInt()
                            )
                            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                            tra_bot.layoutParams = params
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        verticalScrollView.requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                }
                return false
            }
        })

        count++
        myview.tag = count
        verticalScrollLayout.addView(myview)
        Glide.with(mContext).load(image.imageUrl).diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true).into(img)
        ResizeVerticalScroll(myview, img, width, height, image.width, image.height)
    }

    fun hideAllViews() {
        for (i in 1..count) {
            verticalScrollLayout.findViewWithTag<View>(i)
                .findViewById<ImageView>(R.id.cut).visibility = View.GONE
            verticalScrollLayout.findViewWithTag<View>(i)
                .findViewById<RelativeLayout>(R.id.cut_lay).visibility = View.GONE
            verticalScrollLayout.findViewWithTag<View>(i)
                .findViewById<ImageView>(R.id.close).visibility = View.GONE
            verticalScrollLayout.findViewWithTag<View>(i)
                .findViewById<ImageView>(R.id.save).visibility = View.GONE
        }
    }

    fun cropBitmapSC(
        img: ImageView,
        mainLay: View,
        cut_lay: View,
        top_lay: LinearLayout,
        bot_lay: LinearLayout,
        tra_top: ImageView,
        tra_bot: ImageView,
        bot_img: ImageView
    ) {
        val myBit: Bitmap = SRC_Helper.getBitmapFromView(img)
        val y = top_lay.y.toInt()
        val croppedBitmap =
            Bitmap.createBitmap(myBit, 0, y, myBit.width, bot_lay.y.toInt() + bot_img.height - y)
        img.setImageBitmap(croppedBitmap)
        val params = RelativeLayout.LayoutParams(width, 0)
        tra_bot.layoutParams = params
        top_lay.y = 0f
        val params1 = RelativeLayout.LayoutParams(width, 0)
        tra_top.layoutParams = params1
        val params2 = RelativeLayout.LayoutParams(width, croppedBitmap.height)
        cut_lay.layoutParams = params2
        ResizeVerticalScroll(mainLay, img, width, height, croppedBitmap.width, croppedBitmap.height)
    }


    fun ResizeVerticalScroll(
        view: View,
        img: View,
        deviceWidth: Int,
        deviceHeight: Int,
        width: Int,
        height: Int
    ) {

        var deviceHeight = 0
        var newWidth = 0
        var newHeight = 0
        if (height > deviceHeight) {
            deviceHeight = height
        }

        newWidth = deviceWidth
        newHeight = (height * deviceWidth) / width

//        val layoutwidth: Int = deviceWidth
//        val layoutheight: Int = deviceHeight
//        val imagewidth = width
//        val imageheight = height
//        if (imagewidth >= imageheight) {
//            newWidth = layoutwidth
//            newHeight = newWidth * imageheight / imagewidth
//            if (newHeight > layoutheight) {
//                newWidth = layoutheight * newWidth / newHeight
//                newHeight = layoutheight
//            }
//        } else {
//            newHeight = layoutheight
//            newWidth = newHeight * imagewidth / imageheight
//            if (newWidth > layoutwidth) {
//                newHeight = newHeight * layoutwidth / newWidth
//                newWidth = layoutwidth
//            }
//        }

        val params = LinearLayout.LayoutParams(newWidth, newHeight)
        view.layoutParams = params
        val params1 = RelativeLayout.LayoutParams(newWidth, newHeight)
        img.layoutParams = params1
    }

    inner class SaveAsyncTask : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            saveBitmap()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            progressDialog.dismiss()
            startActivity(
                Intent(
                    applicationContext,
                    SRC_ScreenshotPreviewActivity::class.java
                ).putExtra("position", 0)
            )
            finish()

        }

    }

    fun saveBitmap() {
        var bitmap = SRC_Helper.getBitmapFromView(verticalScrollLayout)
        SRC_Helper.saveBitmap(
            "${SRC_FileUtils.getScreenshotDirPath(mContext).absolutePath}${File.separator}${SRC_FileUtils.getFileNameForSS()}",
            bitmap
        )
    }

    fun Back(view: View) {
        onBackPressed()
    }
    var adView: AdView? = null

    private fun loadBanner() {
        val adSize: AdSize = getAdSize()
        adView?.setAdSize(adSize)
        adView?.loadAd(ConsentSDK.getAdRequest(this))
    }

    private fun getAdSize(): AdSize {
        val display: Display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels: Float = outMetrics.widthPixels.toFloat()
        val density: Float = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

}