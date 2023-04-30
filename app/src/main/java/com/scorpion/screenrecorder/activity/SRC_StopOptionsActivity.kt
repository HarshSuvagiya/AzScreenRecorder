package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.scorpion.screenrecorder.R
import kotlinx.android.synthetic.main.src_activity_stop_options.*
import kotlinx.android.synthetic.main.src_count_down_timer_dialog.*
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_Utils


class SRC_StopOptionsActivity : SRC_BaseActivity() {

    lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_stop_options)

        mActivity = this

        SRC_Helper.FS(mActivity)

        initNativeAdvanceAds()
        loadInterstitial()

        initSwitch()
        getTimeLimit()

        setSize()
    }

    fun setSize() {
        SRC_Helper.setSize(header, 1080, 174, true)
        SRC_Helper.setSize(back, 60, 60, true)

        SRC_Helper.setSize(stopOnScreenOffImage, 70, 70, true)
        SRC_Helper.setSize(stopOnShakeImage, 70, 70, true)
        SRC_Helper.setSize(enableTimeLimitImage, 70, 70, true)
        SRC_Helper.setSize(timeLimitImage, 70, 70, true)

        SRC_Helper.setSize(enableStopOnScreenOff, 134, 74, true)
        SRC_Helper.setSize(enableStopOnShake, 134, 74, true)
        SRC_Helper.setSize(enableTimeLimit, 134, 74, true)

        SRC_Helper.setMargin(back, 50, 0, 0, 0)

        SRC_Helper.setMargin(stopOnScreenOffImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(stopOnShakeImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(enableTimeLimitImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(timeLimitImage, 40, 0, 0, 0)

        SRC_Helper.setMargin(enableStopOnScreenOff, 0, 0, 40, 0)
        SRC_Helper.setMargin(enableStopOnShake, 0, 0, 40, 0)
        SRC_Helper.setMargin(enableTimeLimit, 0, 0, 40, 0)
    }

    private fun getTimeLimit() {
        timeLimitLayout.setOnClickListener {
            val dialog = Dialog(this@SRC_StopOptionsActivity)
            dialog.setContentView(R.layout.src_count_down_timer_dialog)
            dialog.show()
            val getSeconds = dialog.findViewById<EditText>(R.id.getSeconds)
            val cancel = dialog.findViewById<TextView>(R.id.cancel)
            val ok = dialog.findViewById<TextView>(R.id.ok)
            val title = dialog.findViewById<TextView>(R.id.title)

            SRC_Helper.setSize(dialog.timeLimit_Popup, 870, 574, true)
            SRC_Helper.setSize(dialog.line, 768, 4, true)
            SRC_Helper.setSize(dialog.editBg, 654, 134, true)

            SRC_Helper.setMargin(dialog.line, 0, 50, 0, 0)
            SRC_Helper.setMargin(dialog.editBg, 0, 50, 0, 0)

            title.text = "Time limit in seconds"

            getSeconds.setText(sharedPreferences.getString("timeLimit", "600"))

            cancel.setOnClickListener { dialog.dismiss() }
            ok.setOnClickListener {
                if (getSeconds.text.toString().length != 0) {
                    timeLimitDetailTV.text = "${getSeconds.text} sec"
                    editor.putString("timeLimit", getSeconds.text.toString())
                    editor.apply()
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        this@SRC_StopOptionsActivity,
                        "Please enter value",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun initSwitch() {
        enableStopOnScreenOff.isChecked =
            sharedPreferences.getBoolean("enableStopOnScreenOff", false)
        enableStopOnShake.isChecked = sharedPreferences.getBoolean("enableStopOnShake", false)
        enableTimeLimit.isChecked = sharedPreferences.getBoolean("enableTimeLimit", false)

        enableStopOnScreenOff.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("enableStopOnScreenOff", isChecked)
            editor.apply()
        }
        enableStopOnShake.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                editor.putBoolean("enableShake", false)
            editor.putBoolean("enableStopOnShake", isChecked)
            editor.apply()
        }
        enableTimeLimit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                timeLimitLayout.visibility = View.VISIBLE
            else
                timeLimitLayout.visibility = View.GONE
            editor.putBoolean("enableTimeLimit", isChecked)
            editor.apply()
        }

        if (sharedPreferences.getBoolean("enableTimeLimit", false))
            timeLimitLayout.visibility = View.VISIBLE
        else
            timeLimitLayout.visibility = View.GONE

    }

    fun Back(view: View) {
        onBackPressed()
    }

    private var adLoader: AdLoader? = null
    private var nativeAdView: UnifiedNativeAdView? = null

    private fun initNativeAdvanceAds() {
        flNativeAds = findViewById(R.id.flNativeAds)
        flNativeAds?.setVisibility(View.GONE)
        nativeAdView = findViewById(R.id.ad_view)
        nativeAdView?.setMediaView(nativeAdView?.findViewById(R.id.ad_media))
        nativeAdView?.setHeadlineView(nativeAdView?.findViewById(R.id.ad_headline))
        nativeAdView?.setBodyView(nativeAdView?.findViewById(R.id.ad_body))
        nativeAdView?.setCallToActionView(nativeAdView?.findViewById(R.id.ad_call_to_action))
        nativeAdView?.setIconView(nativeAdView?.findViewById(R.id.ad_icon))
        nativeAdView?.setStarRatingView(nativeAdView?.findViewById(R.id.ad_stars))
        nativeAdView?.setAdvertiserView(nativeAdView?.findViewById(R.id.ad_advertiser))
        loadNativeAds()
    }

    private fun populateNativeAdView(
        nativeAd: UnifiedNativeAd,
        adView: UnifiedNativeAdView?
    ) {
        val vc = nativeAd.videoController
        vc.videoLifecycleCallbacks = object : VideoLifecycleCallbacks() {
            override fun onVideoEnd() {
                super.onVideoEnd()
            }
        }
        (adView!!.headlineView as TextView).text = nativeAd.headline
        (adView.bodyView as TextView).text = nativeAd.body
        (adView.callToActionView as Button).text = nativeAd.callToAction
        val icon = nativeAd.icon
        if (icon == null) {
            adView.iconView.visibility = View.INVISIBLE
        } else {
            (adView.iconView as ImageView).setImageDrawable(icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }
        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }
        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }
        adView.setNativeAd(nativeAd)
    }

    private var flNativeAds: FrameLayout? = null
    private fun loadNativeAds() {
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(false)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()
        val builder =
            AdLoader.Builder(this, SRC_Utils.NATIVE_ID)
        adLoader = builder.forUnifiedNativeAd { unifiedNativeAd ->
            if (!adLoader!!.isLoading) {
                flNativeAds!!.visibility = View.VISIBLE
                populateNativeAdView(unifiedNativeAd, nativeAdView)
            }
        }.withAdListener(
            object : AdListener() {
                override fun onAdFailedToLoad(errorCode: Int) {
                    Log.e(
                        "StopActivity", "The previous native ad failed to load. Attempting to"
                                + " load another."
                    )
                    if (!adLoader!!.isLoading) {
                    }
                }
            }).withNativeAdOptions(adOptions).build()

        // Load the Native ads.
        adLoader?.loadAd(ConsentSDK.getAdRequest(this))
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
        interstitialAd!!.adUnitId =  SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd!!.loadAd(ConsentSDK.getAdRequest(this))
    }
}