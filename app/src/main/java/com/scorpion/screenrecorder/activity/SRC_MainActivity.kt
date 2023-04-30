package com.scorpion.screenrecorder.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.viewpager.widget.ViewPager
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.src_activity_main.*
import kotlinx.android.synthetic.main.src_main_layout.*
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.adapter.SRC_TabsAdapter
import com.scorpion.screenrecorder.appopen.SRC_AppOpenManager
import com.scorpion.screenrecorder.common.SRC_SdkHelper
import com.scorpion.screenrecorder.utils.SRC_FileUtils
import com.scorpion.screenrecorder.utils.SRC_Utils


class SRC_MainActivity : SRC_BaseActivity(), ViewPager.OnPageChangeListener {

    lateinit var mContext: Context
    private var mStoragePermissionGranted = false
    lateinit var manager: WindowManager
    lateinit var mActivity : Activity
    private var adContainerView: FrameLayout? = null

    private val usableScreenSize: Point
        get() {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            return size
        }

    private val realScreenSize: Point
        get() {
            val size = Point()
            windowManager.defaultDisplay.getRealSize(size)
            return size
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_main)

        mContext = this

        mActivity = this

//        Helper.FS(mActivity)

        SRC_AppOpenManager.needToShow = false

        showOnLock(mContext)

        MobileAds.initialize(
            this
        ) { }

        adContainerView = findViewById(R.id.ad_view_container)
        adView = AdView(this)
        adView?.adUnitId = SRC_Utils.BANNER_ID
        adContainerView?.addView(adView)
        loadBanner()

        btnVideo.setBackgroundResource(R.drawable.src_video_press_button)

        setSize()

        editor.putBoolean("isOverlayGranted",true)
        editor.apply()

        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        Log.d("hasNav", realScreenSize.y.toString())
        Log.d("hasNav2", usableScreenSize.y.toString())

        checkPermissions()

        setupTabsAndViewPager()

        getOverlayPermission()

        stitchImage.setOnClickListener{
            startActivity(Intent(applicationContext, SRC_ImageFolderActivity::class.java))
        }

        btnVideo.setOnClickListener{
            viewPager.currentItem = 0
        }
        btnScrrenshot.setOnClickListener{
            viewPager.currentItem = 1
        }
        btnSetting.setOnClickListener{
            viewPager.currentItem = 2
        }

    }

    fun setSize(){

        SRC_Helper.setSize(header,1080,152,true)
        SRC_Helper.setSize(back,60,60,true)
        SRC_Helper.setSize(laytap, 1080, 132)
        SRC_Helper.setSize(btnVideo, 360, 150)
        SRC_Helper.setSize(btnScrrenshot, 360, 150)
        SRC_Helper.setSize(btnSetting, 360, 150)
        SRC_Helper.setSize(stitchImage, 60, 60)

        SRC_Helper.setMargin(back,50,0,0,0)
        SRC_Helper.setMargin(stitchImage,0,0,50,0)

    }

    private fun getOverlayPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 10)
            return
        }
    }

    private fun setupTabsAndViewPager() {
        val tabsAdapter = SRC_TabsAdapter(this, supportFragmentManager)
        viewPager.adapter = tabsAdapter
        viewPager.setOnPageChangeListener(this@SRC_MainActivity)
    }

    private fun checkPermissions() {
        if (SRC_SdkHelper.atleastM()) {
            mStoragePermissionGranted = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!mStoragePermissionGranted) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    ),
                    PERMISSION_REQUESTS
                )
            }
            else{
                SRC_FileUtils.makeRootDir(this@SRC_MainActivity)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUESTS -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    SRC_FileUtils.makeRootDir(this@SRC_MainActivity)
                }
            }
        }
    }

    companion object {
        const val PERMISSION_REQUESTS = 24
    }

    fun Back(view: View) {
        onBackPressed()
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {

        when (position) {
            0 -> {
                stitchImage.visibility = View.GONE
                txtTitle.text = "Videos"
                btnVideo.setBackgroundResource(R.drawable.src_video_press_button)
                btnScrrenshot.setBackgroundResource(R.drawable.src_screenshot_button)
                btnSetting.setBackgroundResource(R.drawable.src_setting_button)
            }
            1 -> {
                stitchImage.visibility = View.GONE
                txtTitle.text = "Screenshots"
                btnVideo.setBackgroundResource(R.drawable.src_video_button)
                btnScrrenshot.setBackgroundResource(R.drawable.src_screenshot_press_button)
                btnSetting.setBackgroundResource(R.drawable.src_setting_button)
            }
            2 -> {
                stitchImage.visibility = View.GONE
                txtTitle.text = "Settings"
                btnVideo.setBackgroundResource(R.drawable.src_video_button)
                btnScrrenshot.setBackgroundResource(R.drawable.src_screenshot_button)
                btnSetting.setBackgroundResource(R.drawable.src_setting_press_button)
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
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

    @SuppressLint("WrongConstant")
    fun showOnLock(context: Context) {
        try {
            val intent =
                Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            intent.putExtra("extra_pkgname", context.packageName)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.flags = 268468224
            intent.addFlags(1073741824)
            intent.addFlags(8388608)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
