package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.ParseException
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.*
import com.scorpion.screenrecorder.R
import kotlinx.android.synthetic.main.src_activity_screenshot_preview.back
import kotlinx.android.synthetic.main.src_activity_video_view.*
import kotlinx.android.synthetic.main.src_activity_video_view.edit
import kotlinx.android.synthetic.main.src_activity_video_view.topBar
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_Utils
import java.io.File
import java.util.concurrent.TimeUnit


class SRC_VideoViewActivity : AppCompatActivity() {
    lateinit var mActivity: Activity
    lateinit var dialog: Dialog
    var path: String = ""
    var isVideoPause: Boolean = false
    var duration: Int = 0
    var handler = Handler()
    var currentProgress : Int = 0
    var seekRunnable: Runnable = object : Runnable {
        override fun run() {
            if (videoView != null && videoView.isPlaying) {
                var currentPos = videoView.currentPosition
                seekBar.progress = currentPos
                currentDuration.text =  formatTimeUnit(currentPos.toLong())
                handler.postDelayed(this, 100)
            }
        }
    }
    private var adContainerView: FrameLayout? = null

    override fun onResume() {
        super.onResume()

        SRC_Helper.FS2(mActivity)

        path = intent.getStringExtra("path")!!
        videoView.setVideoPath(path)
        videoView.setOnPreparedListener { mp ->
            Log.d("duration123", mp!!.duration.toString())
            //                videoView.seekTo(currentProgress)
            duration = mp!!.duration
            seekBar.max = duration
            videoView.start()
            handler.postDelayed(seekRunnable, 100)
        }
        videoView.setOnCompletionListener {
            videoView.seekTo(0)
            videoView.start()
            handler.postDelayed(seekRunnable, 100)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_video_view)

        mActivity = this

        SRC_Helper.FS(mActivity)

        MobileAds.initialize(
            this
        ) { }

        adContainerView = findViewById<FrameLayout>(R.id.ad_view_container)
// Step 1 - Create an AdView and set the ad unit ID on it.
// Step 1 - Create an AdView and set the ad unit ID on it.
        adView = AdView(this)
        adView?.adUnitId = SRC_Utils.BANNER_ID
        adContainerView?.addView(adView)
        loadBanner()

        loadInterstitial()

//        setSize()

        playPause.setOnClickListener {
            if (videoView.isPlaying) {
                isVideoPause = true
                videoView.pause()
                handler.removeCallbacks(seekRunnable)
                playPause.setImageResource(R.drawable.src_play_button)
            } else {
                handler.postDelayed(seekRunnable, 100)
                videoView.start()
                playPause.setImageResource(R.drawable.src_pause_button)
            }
        }
        edit.setOnClickListener {
            startActivity(Intent(applicationContext,SRC_VideoEditActivity::class.java).putExtra("path",path))
            finish()
        }

//        seekBar.isEnabled = false
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    currentProgress = progress
                    Log.d("fromUser","fromUser1")
//                    videoView.pause()
                    videoView.seekTo(progress)
//                    videoView.resume()
                    Log.d("fromUser",progress.toString())
                    Log.d("fromUser","fromUser2")
                    currentDuration.text = formatTimeUnit(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
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
    }

    fun setSize(){
        SRC_Helper.setSize(topBar,1080,152,true)
        SRC_Helper.setSize(back,60,60,true)
        SRC_Helper.setSize(edit,60,60,true)
        SRC_Helper.setSize(videoBG,994,1422,true)
        SRC_Helper.setSize(delete,140,190,true)
        SRC_Helper.setSize(share,140,190,true)
        SRC_Helper.setSize(playPause,100,100,true)
        SRC_Helper.setSize(bottomLayout,994,182,true)

        SRC_Helper.setMargin(back,50,0,0,0)
        SRC_Helper.setMargin(edit,0,0,50,0)
//        Helper.setMargin(delete,0,50,0,0)
//        Helper.setMargin(share,0,50,0,0)
    }

    @Throws(ParseException::class)
    fun formatTimeUnit(j: Long): String? {
        return String.format(
            "%02d:%02d",
            *arrayOf<Any>(
                java.lang.Long.valueOf(
                    TimeUnit.MILLISECONDS.toMinutes(j)
                ),
                java.lang.Long.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(j) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(j)
                    )
                )
            )
        )
    }

    fun delete() {
        dialog = Dialog(this@SRC_VideoViewActivity)
        dialog.setContentView(R.layout.src_delete_dialog)
        dialog.show()
        val delete_Popup: ConstraintLayout = dialog.findViewById(R.id.delete_Popup)
        val delete_Icon: ImageView = dialog.findViewById(R.id.delete_Icon)
        val no: ImageView = dialog.findViewById(R.id.no)
        val yes: ImageView = dialog.findViewById(R.id.yes)
        val alert: TextView = dialog.findViewById(R.id.alert)

        SRC_Helper.setSize(delete_Popup,870,666,true)
        SRC_Helper.setSize(delete_Icon,102,124,true)
        SRC_Helper.setSize(no,324,114,true)
        SRC_Helper.setSize(yes,324,114,true)

        SRC_Helper.setMargin(delete_Icon,0,80,0,0)


        alert.text = "Do you want to delete this Video?"
        no.setOnClickListener { dialog.dismiss() }
        yes.setOnClickListener {
            val fdelete =
                File(path)
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
            File(path)
        )
        share.putExtra("android.intent.extra.STREAM", pathUri)
        startActivity(Intent.createChooser(share, "Share"))
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
        val widthPixels: Int = outMetrics.widthPixels
        val density: Float = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }
    override fun onBackPressed() {
//        super.onBackPressed()
//        finish()
        videoView.pause()
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
        interstitialAd!!.adUnitId =  SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd!!.loadAd(ConsentSDK.getAdRequest(this))
    }
}