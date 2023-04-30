package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Display
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.src_activity_grant_overlay_permission.*
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_Utils


class SRC_GrantOverlayPermissionActivity : SRC_BaseActivity() {

    lateinit var mActivity: Activity
    var adContainerView: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_grant_overlay_permission)

        mActivity = this

        SRC_Helper.FS(mActivity)

        MobileAds.initialize(
            this
        ) { }

        adContainerView = findViewById(R.id.ad_view_container)
        adView = AdView(this)
        adView?.adUnitId = SRC_Utils.BANNER_ID
        adContainerView?.addView(adView)
        loadBanner()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(applicationContext)) {
                startActivity(Intent(applicationContext, SRC_MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(applicationContext, SRC_MainActivity::class.java))
            finish()
        }

        grant.setOnClickListener {
            getOverlayPermission()
        }

        setSize()
    }

    fun setSize() {
        SRC_Helper.setSize(titles, 690, 122, true)
        SRC_Helper.setSize(icon, 600, 600, true)
        SRC_Helper.setSize(grant, 506, 150, true)
        SRC_Helper.setMargin(titles, 0, 150, 0, 0)
        SRC_Helper.setMargin(icon, 0, 100, 0, 0)
        SRC_Helper.setMargin(grant, 0, 100, 0, 0)
    }

    private fun getOverlayPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())
            )
            startActivityForResult(intent, 10)
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10) {
            if (Settings.canDrawOverlays(applicationContext)) {
                startActivity(Intent(applicationContext, SRC_MainActivity::class.java))
                finish()
            }
        }
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
}