package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.ayoubfletcher.consentsdk.ConsentSDK.ConsentCallback
import com.google.android.gms.ads.*
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import kotlinx.android.synthetic.main.src_activity_start.*
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.receiver.SRC_ScreenReceiver
import com.scorpion.screenrecorder.utils.SRC_Utils
import java.util.*

class SRC_StartActivity : AppCompatActivity() {

    lateinit var mActivity: Activity
    lateinit var adContainerView: FrameLayout
    companion object {
        lateinit var unifiedNativeAd1: UnifiedNativeAd
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_start)

        mActivity = this

        SRC_Helper.FS(mActivity)

        MobileAds.initialize(
            this
        ) { }

        adContainerView = findViewById(R.id.ad_view_container)
        adView = AdView(this)
        adView?.adUnitId = SRC_Utils.BANNER_ID
        adContainerView.addView(adView)

        initConsentSDK(this)
        if (!isConsentDone() && isNetworkAvailable() && ConsentSDK.isUserLocationWithinEea(this)) {
//        if(!isConsentDone()&& isNetworkAvailable()){
            consentSDK?.checkConsent(object : ConsentCallback() {
                override fun onResult(isRequestLocationInEeaOrUnknown: Boolean) {
                    setPref()
                    ConsentSDK.Builder.dialog.dismiss()
                    goToMain()
                }
            })
        } else {
            goToMain()
        }

        setSize()

        start?.setOnClickListener {
//            startActivity(Intent(applicationContext, GrantOverlayPermissionActivity::class.java))

            if (interstitialAd!!.isLoaded) {
                interstitialAd!!.adListener = object : AdListener() {
                    override fun onAdClosed() {
                        startActivity(
                            Intent(
                                applicationContext,
                                SRC_GrantOverlayPermissionActivity::class.java
                            )
                        )
                        loadInterstitial()
                    }
                }
                interstitialAd!!.show()
            } else {
                startActivity(
                    Intent(
                        applicationContext,
                       SRC_GrantOverlayPermissionActivity::class.java
                    )
                )
            }
        }

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        val mReceiver: BroadcastReceiver = SRC_ScreenReceiver()
        registerReceiver(mReceiver, filter)
    }

    fun setSize() {
        SRC_Helper.setSize(logo, 1080, 928, true)
        SRC_Helper.setSize(start, 600, 250, true)
        SRC_Helper.setSize(shareButton, 280, 180, true)
        SRC_Helper.setSize(rateButton, 280, 180, true)
        SRC_Helper.setSize(infoButton, 280, 180, true)

    }

    fun rateApp(view: View?) {
        try {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: Exception) {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    fun shareApp(view: View?) {
        try {
            val intent =
                Intent("android.intent.action.SEND")
            intent.type = "text/plain"
            intent.putExtra(
                "android.intent.extra.SUBJECT", "Have a look at " +
                        getString(R.string.app_name) + " app "
            )
            intent.putExtra(
                "android.intent.extra.TEXT",
                """Hey Check out this new ${getString(R.string.app_name)} App - ${getString(R.string.app_name)} 
Available on Google play store,You can also download it from "https://play.google.com/store/apps/details?id=$packageName""""
            )
            startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
        }
    }

    fun openPrivacyPolicy(view: View?) {

        if(isNetworkAvailable()){

            startActivity(
                Intent(
                    applicationContext,
                    SRC_PrivacyPolicy::class.java
                )
            )
        }else{
            Toast.makeText(mActivity, "Please Connect to Internet", Toast.LENGTH_SHORT).show()
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

    private var consentSDK: ConsentSDK? = null

    private fun initConsentSDK(context: Context) {
        // Initialize ConsentSDK
        consentSDK =
            ConsentSDK.Builder(this) //                .addTestDeviceId("77259D4779E9E87A669924752B4E3B2B")
                .addCustomLogTag("CUSTOM_TAG") // Add custom tag default: ID_LOG
                .addPrivacyPolicy(getString(R.string.privacy_link)) // Add your privacy policy url
                .addPublisherId(getString(R.string.admob_publisher_id)) // Add your admob publisher id
                .build()
    }

    private fun goToMain() {
        loadInterstitial()

        loadBanner()

        initNativeAdvanceAds()

        loadInterstitialExit()
    }

    fun setPref() {
        val editor: SharedPreferences.Editor =
            getSharedPreferences("consentpreff", Context.MODE_PRIVATE).edit()
        editor.putBoolean("isDone", true)
        editor.apply()
    }

    fun isConsentDone(): Boolean {
        val prefs: SharedPreferences =
            getSharedPreferences("consentpreff", Context.MODE_PRIVATE)
        return prefs.getBoolean("isDone", false)
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
    override fun onResume() {
        super.onResume()
        SRC_Helper.FS2(mActivity)
    }
    private var adLoader: AdLoader? = null
    private val mNativeAds: List<UnifiedNativeAd> =
        ArrayList()
    var nativeAdView: UnifiedNativeAdView? = null

    private fun initNativeAdvanceAds() {
        flNativeAds = findViewById(R.id.flNativeAds)
        flNativeAds?.setVisibility(View.GONE)
        nativeAdView = findViewById(R.id.ad_view)
        nativeAdView?.setMediaView(nativeAdView?.findViewById<View>(R.id.ad_media) as MediaView)
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

// Assign native ad object to the native view.
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
        adLoader =
            builder.forUnifiedNativeAd { unifiedNativeAd -> // A native ad loaded successfully, check if the ad loader has finished loading
                // and if so, insert the ads into the list.
                //						mNativeAds.add(unifiedNativeAd);
                if (!adLoader!!.isLoading) {
                    logo.visibility = View.INVISIBLE
                    flNativeAds!!.visibility = View.VISIBLE
                    unifiedNativeAd1 =
                        unifiedNativeAd
                    populateNativeAdView(unifiedNativeAd, nativeAdView)
                }
            }.withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: Int) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Log.e(
                            "MainActivity", "The previous native ad failed to load. Attempting to"
                                    + " load another."
                        )
                        if (!adLoader!!.isLoading) {
                        }
                    }
                }).withNativeAdOptions(adOptions).build()

        // Load the Native ads.
        adLoader?.loadAd(ConsentSDK.getAdRequest(this))
    }

    var interstitialAd1: InterstitialAd? = null

    // Load Interstitial
    private fun loadInterstitialExit() {
        interstitialAd1 = InterstitialAd(this)
        interstitialAd1!!.adUnitId =  SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd1!!.loadAd(ConsentSDK.getAdRequest(this))
    }

    var adView: AdView? = null

    private fun loadBanner() {
        val adSize = getAdSize()
        adView!!.adSize = adSize
        adView!!.loadAd(ConsentSDK.getAdRequest(this))
    }

    private fun getAdSize(): AdSize {
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            this,
            adWidth
        )
    }

    override fun onBackPressed() {
//        startActivity(new Intent(getApplicationContext(),MORNINGGLORYAPPS_Exit.class));
        if (interstitialAd1!!.isLoaded) {
            interstitialAd1!!.adListener = object : AdListener() {
                override fun onAdClosed() {
                    startActivity(Intent(applicationContext, SRC_ExitActivity::class.java))
                    finish()
                    loadInterstitialExit()
                }
            }
            interstitialAd1!!.show()
        } else {
            startActivity(Intent(applicationContext, SRC_ExitActivity::class.java))
            finish()
        }
    }
}