package com.scorpion.screenrecorder.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.DisplayMetrics
import android.view.Display
import android.view.View
import android.widget.*
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.*
import com.scorpion.screenrecorder.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.src_activity_text_and_logo.*
import kotlinx.android.synthetic.main.src_count_down_timer_dialog.*
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_Utils
import yuku.ambilwarna.AmbilWarnaDialog


class SRC_TextAndLogoActivity : SRC_BaseActivity() {

    lateinit var mActivity : Activity
    private var adContainerView: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_text_and_logo)

        mActivity = this

        SRC_Helper.FS(mActivity)

        loadInterstitial()

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

        textSizeDetailTV.setText(sharedPreferences.getString("textSize", "25"))
        textDetailTV.setText(sharedPreferences.getString("textInput", getString(R.string.app_name)))
        colorForText.setBackgroundColor(sharedPreferences.getInt("colorForText", 0))
        enableText.isChecked = sharedPreferences.getBoolean("enableText", false)
        enableLogo.isChecked = sharedPreferences.getBoolean("enableLogo", false)
        colorForBackgroundText.setBackgroundColor(
            sharedPreferences.getInt(
                "colorForBackgroundText",
                0
            )
        )
        logoSizeDetailTV.text = "${(sharedPreferences.getInt("logoSize", 20))} %"


        textColorLayout.setOnClickListener {
            AmbilWarnaDialog(
                this@SRC_TextAndLogoActivity,
                sharedPreferences.getInt("colorForText", 0),
                true,
                object :
                    AmbilWarnaDialog.OnAmbilWarnaListener {
                    override fun onCancel(dialog: AmbilWarnaDialog?) {
                        dialog?.dialog?.dismiss()
                    }

                    override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {

                        colorForText.setBackgroundColor(color)
                        editor.putInt("colorForText", color)
                        editor.apply()

                        val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
                        intent.putExtra("whichFloating", "floatingTextViewColor")
                        sendBroadcast(intent)
                        dialog?.dialog?.dismiss()
                    }
                }).show()
        }

        backgroundColorLayout.setOnClickListener {
            AmbilWarnaDialog(this@SRC_TextAndLogoActivity, R.color.tabSelectedIconColor, true, object :
                AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    dialog?.dialog?.dismiss()
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    colorForBackgroundText.setBackgroundColor(color)
                    editor.putInt("colorForBackgroundText", color)
                    editor.apply()

                    val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
                    intent.putExtra("whichFloating", "floatingTextViewBGColor")
                    sendBroadcast(intent)

                    dialog?.dialog?.dismiss()
                }

            }).show()
        }

        textSizeLayout.setOnClickListener {
            openTextSizeDialog()
        }
        textLayout.setOnClickListener {
            openTextInputDialog()
        }
        logoSizeLayout.setOnClickListener {
            openLogoSizeDialog()
        }

        enableText.setOnCheckedChangeListener { _, isChecked ->

            editor.putBoolean("enableText", isChecked)
            editor.apply()

            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("turn_on_off_floating_text_View", isChecked)
            intent.putExtra("whichFloating", "floatingTextView")
            sendBroadcast(intent)
        }

        enableLogo.setOnCheckedChangeListener { _, isChecked ->

            editor.putBoolean("enableLogo", isChecked)
            editor.apply()

            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("turn_on_off_floating_logo", isChecked)
            intent.putExtra("whichFloating", "floatingLogo")
            sendBroadcast(intent)
        }

        imageLayout.setOnClickListener {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(this);
        }
    }

    fun setSize(){
        SRC_Helper.setSize(header,1080,152,true)
        SRC_Helper.setSize(back,60,60,true)
        SRC_Helper.setSize(next_arrow,40,40,true)
        SRC_Helper.setSize(next_arrow1,40,40,true)
        SRC_Helper.setSize(next_arrow2,40,40,true)
        SRC_Helper.setSize(next_arrow3,40,40,true)

        SRC_Helper.setSize(color_bg,80,80,true)
        SRC_Helper.setSize(colorForText,80,80,true)
        SRC_Helper.setSize(bgColor,80,80,true)
        SRC_Helper.setSize(colorForBackgroundText,80,80,true)

        SRC_Helper.setSize(showTextImage,70,70,true)
        SRC_Helper.setSize(textImage,70,70,true)
        SRC_Helper.setSize(textColorImage,70,70,true)
        SRC_Helper.setSize(bgColorImage,70,70,true)
        SRC_Helper.setSize(textSizeImage,70,70,true)
        SRC_Helper.setSize(showLogoImage,70,70,true)
        SRC_Helper.setSize(imageImage,70,70,true)
        SRC_Helper.setSize(sizeImage,70,70,true)

        SRC_Helper.setSize(enableText,134,74,true)
        SRC_Helper.setSize(enableLogo,134,74,true)

        SRC_Helper.setMargin(back,50,0,0,0)

        SRC_Helper.setMargin(showTextImage,40,0,0,0)
        SRC_Helper.setMargin(textImage,40,0,0,0)
        SRC_Helper.setMargin(textColorImage,40,0,0,0)
        SRC_Helper.setMargin(bgColorImage,40,0,0,0)
        SRC_Helper.setMargin(textSizeImage,40,0,0,0)
        SRC_Helper.setMargin(showLogoImage,40,0,0,0)
        SRC_Helper.setMargin(imageImage,40,0,0,0)
        SRC_Helper.setMargin(sizeImage,40,0,0,0)

        SRC_Helper.setMargin(enableText,0,0,40,0)
        SRC_Helper.setMargin(next_arrow,0,0,40,0)
        SRC_Helper.setMargin(color_bg,0,0,40,0)
        SRC_Helper.setMargin(colorForText,0,0,40,0)
        SRC_Helper.setMargin(bgColor,0,0,40,0)
        SRC_Helper.setMargin(colorForBackgroundText,0,0,40,0)
        SRC_Helper.setMargin(next_arrow1,0,0,40,0)
        SRC_Helper.setMargin(enableLogo,0,0,40,0)
        SRC_Helper.setMargin(next_arrow2,0,0,40,0)
        SRC_Helper.setMargin(next_arrow3,0,0,40,0)
    }

    private fun openTextInputDialog() {
        val dialog = Dialog(this@SRC_TextAndLogoActivity)
        dialog.setContentView(R.layout.src_count_down_timer_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
        val getSeconds = dialog.findViewById<EditText>(R.id.getSeconds)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val ok = dialog.findViewById<TextView>(R.id.ok)
        val title = dialog.findViewById<TextView>(R.id.title)

        SRC_Helper.setSize(dialog.timeLimit_Popup,870,574,true)
        SRC_Helper.setSize(dialog.line,768,4,true)
        SRC_Helper.setSize(dialog.editBg,654,134,true)

        SRC_Helper.setMargin(dialog.line,0,50,0,0)
        SRC_Helper.setMargin(dialog.editBg,0,50,0,0)

        title.text = "Text"
        getSeconds.setText(sharedPreferences.getString("textInput", getString(R.string.app_name)))
        getSeconds.hint = "Enter text"
        getSeconds.inputType = InputType.TYPE_CLASS_TEXT
        cancel.setOnClickListener { dialog.dismiss() }
        ok.setOnClickListener {
            if (getSeconds.text.toString().isNotEmpty()) {
                textDetailTV.text = "${getSeconds.text}"
                editor.putString("textInput", getSeconds.text.toString())
                editor.apply()
                dialog.dismiss()
            } else {
                Toast.makeText(applicationContext, "Please enter value", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openTextSizeDialog() {
        val dialog = Dialog(this@SRC_TextAndLogoActivity)
        dialog.setContentView(R.layout.src_count_down_timer_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
        val getSeconds = dialog.findViewById<EditText>(R.id.getSeconds)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val ok = dialog.findViewById<TextView>(R.id.ok)
        val title = dialog.findViewById<TextView>(R.id.title)

        SRC_Helper.setSize(dialog.timeLimit_Popup,870,574,true)
        SRC_Helper.setSize(dialog.line,768,4,true)
        SRC_Helper.setSize(dialog.editBg,654,134,true)

        SRC_Helper.setMargin(dialog.line,0,50,0,0)
        SRC_Helper.setMargin(dialog.editBg,0,50,0,0)

        title.text = "Text size"
        getSeconds.setText(sharedPreferences.getString("textSize", "25"))

        cancel.setOnClickListener { dialog.dismiss() }
        ok.setOnClickListener {
            if (getSeconds.text.toString().length != 0) {
                if (getSeconds.text.toString().toInt() > 7 && getSeconds.text.toString()
                        .toInt() < 70
                ) {
                    val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
                    intent.putExtra("whichFloating", "floatingTextViewSize")
                    intent.putExtra("size", getSeconds.text.toString())
                    sendBroadcast(intent)

                    textSizeDetailTV.text = "${getSeconds.text}"
                    editor.putString("textSize", getSeconds.text.toString())
                    editor.apply()
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Enter value between 7 to 70",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, "Please enter value", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openLogoSizeDialog() {
        val dialog = Dialog(this@SRC_TextAndLogoActivity)
        dialog.setContentView(R.layout.src_logo_resizer_layout)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
        val seekBar = dialog.findViewById<SeekBar>(R.id.seekBar)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        var seekBarProgress = dialog.findViewById<TextView>(R.id.seekBarProgress)
        val ok = dialog.findViewById<TextView>(R.id.ok)

        seekBar.progress = sharedPreferences.getInt("logoSize", 20)
        seekBarProgress.text = "${sharedPreferences.getInt("logoSize", 20)} %"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarProgress.text = "${progress} %"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        cancel.setOnClickListener { dialog.dismiss() }
        ok.setOnClickListener {
            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("whichFloating", "floatingLogoSize")
            sendBroadcast(intent)

            logoSizeDetailTV.text = "${seekBar.progress} %"
            editor.putInt("logoSize", seekBar.progress)
            editor.apply()
            dialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                editor.putString("pathOfLogo", result.uri.toString())
                editor.apply()

                val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
                intent.putExtra("whichFloating", "floatingLogoChange")
                sendBroadcast(intent)

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
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
        val widthPixels: Int = outMetrics.widthPixels
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
        interstitialAd!!.adUnitId =  SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd!!.loadAd(ConsentSDK.getAdRequest(this))
    }
}