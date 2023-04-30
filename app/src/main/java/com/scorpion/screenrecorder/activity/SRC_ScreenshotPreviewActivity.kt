package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.viewpager.widget.ViewPager
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.*
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.adapter.SRC_ImageViewPagerAdapter
import kotlinx.android.synthetic.main.src_activity_screenshot_preview.*
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.imageeditor.SRC_EditImageActivity
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenshotDirPath
import com.scorpion.screenrecorder.utils.SRC_Utils
import java.io.File
import java.util.*


class SRC_ScreenshotPreviewActivity : AppCompatActivity() {

    var finalPosition: Int = 0
    var fileArrayList = ArrayList<File>()
    var imageViewPagerAdapter: SRC_ImageViewPagerAdapter? = null
    lateinit var dialog: Dialog
    lateinit var mActivity: Activity
    private var adContainerView: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_screenshot_preview)

        mActivity = this

        SRC_Helper.FS(mActivity)

        loadInterstitial()

        MobileAds.initialize(
            this
        ) { }

        adContainerView = findViewById(R.id.ad_view_container)
        adView = AdView(this)
        adView?.adUnitId = SRC_Utils.BANNER_ID
        adContainerView?.addView(adView)
        loadBanner()

//        setSize()

        finalPosition = intent.getIntExtra("position", 0)

        screenShotViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                finalPosition = position
            }

            override fun onPageSelected(position: Int) {

            }

        })

        delete.setOnClickListener {
            delete()
        }
        share.setOnClickListener {
            share()
        }
        back.setOnClickListener {
            onBackPressed()
        }
        edit.setOnClickListener {
            startActivity(
                Intent(applicationContext, SRC_EditImageActivity::class.java).putExtra(
                    "uri",
                    fileArrayList.get(finalPosition).absolutePath
                )
            )
            finish()
        }
    }

    fun setSize() {
        SRC_Helper.setSize(topBar, 1080, 152, true)
        SRC_Helper.setSize(back, 60, 60, true)
        SRC_Helper.setSize(edit, 70, 70, true)
        SRC_Helper.setSize(delete, 140, 190, false)
        SRC_Helper.setSize(share, 140, 190, false)

        SRC_Helper.setMargin(back, 50, 0, 0, 0)
        SRC_Helper.setMargin(edit, 0, 0, 50, 0)
        SRC_Helper.setMargin(delete, 0, 50, 0, 0)
        SRC_Helper.setMargin(share, 0, 50, 0, 0)
    }

    override fun onResume() {
        super.onResume()

        SRC_Helper.FS2(mActivity)

        DataLoader().execute()
    }

    fun getScreenShots() {
        lateinit var listFiles: Array<File>
        fileArrayList.clear()
        val file = getScreenshotDirPath(this)
        if (file.exists()) {
            listFiles = file.listFiles()
            if (listFiles != null) {
                for (files in listFiles) {
                    fileArrayList.add(files)
                }
            }
        }
    }

    fun delete() {
        dialog = Dialog(this@SRC_ScreenshotPreviewActivity)
        dialog.setContentView(R.layout.src_delete_dialog)
        dialog.show()

        val delete_Popup: ConstraintLayout = dialog.findViewById(R.id.delete_Popup)
        val delete_Icon: ImageView = dialog.findViewById(R.id.delete_Icon)
        val no: ImageView = dialog.findViewById(R.id.no)
        val yes: ImageView = dialog.findViewById(R.id.yes)
        val alert: TextView = dialog.findViewById(R.id.alert)

        SRC_Helper.setSize(delete_Popup, 870, 666, true)
        SRC_Helper.setSize(delete_Icon, 102, 124, true)
        SRC_Helper.setSize(no, 324, 114, true)
        SRC_Helper.setSize(yes, 324, 114, true)

        SRC_Helper.setMargin(delete_Icon, 0, 80, 0, 0)

        alert.text = "Do you want to delete this image ?"
        no.setOnClickListener { dialog.dismiss() }
        yes.setOnClickListener {
            val fdelete =
                File(fileArrayList.get(finalPosition).absolutePath)
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    finish()
                } else {
                    dialog.cancel()
                }
            }
        }
    }

    fun share() {
        val share = Intent("android.intent.action.SEND")
        share.type = "image/*"
        val pathUri = FileProvider.getUriForFile(
            applicationContext,
            "$packageName.provider",
            File(fileArrayList.get(finalPosition).absolutePath)
        )
        share.putExtra("android.intent.extra.STREAM", pathUri)
        startActivity(Intent.createChooser(share, "Share"))
    }

    inner class DataLoader : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            getScreenShots()

            fileArrayList.sortWith { o1, o2 ->
                val s11 = o1?.lastModified()
                val s22 = o2?.lastModified()
                s22!!.compareTo(s11!!)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            imageViewPagerAdapter = SRC_ImageViewPagerAdapter(fileArrayList, this@SRC_ScreenshotPreviewActivity)
            screenShotViewPager.adapter = imageViewPagerAdapter
            screenShotViewPager.currentItem = finalPosition
        }

    }

    fun Back(view: View) {
        onBackPressed()
    }

    var adView: AdView? = null

    private fun loadBanner() {
        val adSize: AdSize = getAdSize()
        adView!!.adSize = adSize
        adView!!.loadAd(ConsentSDK.getAdRequest(this))
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

    override fun onBackPressed() {
//        super.onBackPressed()
//        finish()

        if (interstitialAd?.isLoaded()!!) {
            interstitialAd?.setAdListener(object : AdListener() {
                override fun onAdClosed() {
                    finish()
                }
            })
            interstitialAd?.show()
        } else {
            super.onBackPressed()
            finish()
        }
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