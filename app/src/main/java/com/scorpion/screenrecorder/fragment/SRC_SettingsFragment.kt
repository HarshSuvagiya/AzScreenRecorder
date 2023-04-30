package com.scorpion.screenrecorder.fragment

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.activity.SRC_StopOptionsActivity
import com.scorpion.screenrecorder.activity.SRC_TextAndLogoActivity
import com.scorpion.screenrecorder.service.SRC_ScreenshotFloatingButtonService
import kotlinx.android.synthetic.main.src_count_down_timer_dialog.*
import kotlinx.android.synthetic.main.src_fragment_settings.*
import com.scorpion.screenrecorder.SRC_Helper


class SRC_SettingsFragment : SRC_BaseFragment() {

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.src_fragment_settings, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSize()

        enableFloating.isChecked = isMyServiceRunning(SRC_ScreenshotFloatingButtonService::class.java)

        initSwitches()
        getSwitchClick()
        onClickLayout()
        countDownTimeLayout.setOnClickListener {
            openCountDownTimerDialog()
        }
        stopOptionsLayout.setOnClickListener {
            openStopOptionsActivity()
        }
        textAndLogoLayout.setOnClickListener {
            openTextAndLogoActivity()
        }
    }


    fun setSize() {
        SRC_Helper.setSize(serviceLayout, 1006, 188, true)
        SRC_Helper.setSize(serviceImage, 120, 120, true)
        SRC_Helper.setSize(enableFloating, 134, 74, true)
        SRC_Helper.setSize(enableCountDown, 134, 74, true)
        SRC_Helper.setSize(enableCamera, 134, 74, true)
        SRC_Helper.setSize(enableFloatingScreenShot, 134, 74, true)
        SRC_Helper.setSize(enableScreenshotSound, 134, 74, true)
        SRC_Helper.setSize(enableShake, 134, 74, true)
        SRC_Helper.setSize(enableExcludeStatus, 134, 74, true)
        SRC_Helper.setSize(enableExcludeNavigation, 134, 74, true)
        SRC_Helper.setSize(enableShowPreviewDialog, 134, 74, true)
        SRC_Helper.setSize(enableBrush, 134, 74, true)

        SRC_Helper.setSize(videoResolutionImage, 70, 70, true)
        SRC_Helper.setSize(videoBitRateImage, 70, 70, true)
        SRC_Helper.setSize(videoFrameRateImage, 70, 70, true)
        SRC_Helper.setSize(audioBitRateImage, 70, 70, true)
        SRC_Helper.setSize(audioSampleRateImage, 70, 70, true)
        SRC_Helper.setSize(stopOptionsImage, 70, 70, true)
        SRC_Helper.setSize(countDownImage, 70, 70, true)
        SRC_Helper.setSize(countDownTimeImage, 70, 70, true)
        SRC_Helper.setSize(cameraImage, 70, 70, true)
        SRC_Helper.setSize(floatingScreenShotImage, 70, 70, true)
        SRC_Helper.setSize(resolutionImage, 70, 70, true)
        SRC_Helper.setSize(shakeImage, 70, 70, true)
        SRC_Helper.setSize(excludeStatusImage, 70, 70, true)
        SRC_Helper.setSize(excludeNavigationImage, 70, 70, true)
        SRC_Helper.setSize(showPreviewDialogImage, 70, 70, true)
        SRC_Helper.setSize(brushImage, 70, 70, true)
        SRC_Helper.setSize(textAndLogoImage, 70, 70, true)

        SRC_Helper.setSize(r_arrow, 34, 34, true)
        SRC_Helper.setSize(bit_arrow, 34, 34, true)
        SRC_Helper.setSize(frame_arrow, 34, 34, true)
        SRC_Helper.setSize(a_bit_arrow, 34, 34, true)
        SRC_Helper.setSize(a_sample_arrow, 34, 34, true)
        SRC_Helper.setSize(stop_arrow, 34, 34, true)
        SRC_Helper.setSize(count_val_arrow, 34, 34, true)

        SRC_Helper.setMargin(serviceLayout, 0, 40, 0, 0)
        SRC_Helper.setMargin(serviceImage, 20, 0, 0, 0)
        SRC_Helper.setMargin(enableFloating, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableCountDown, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableCamera, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableFloatingScreenShot, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableScreenshotSound, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableShake, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableExcludeStatus, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableExcludeNavigation, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableShowPreviewDialog, 0, 0, 20, 0)
        SRC_Helper.setMargin(enableBrush, 0, 0, 20, 0)

        SRC_Helper.setMargin(videoResolutionImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(videoBitRateImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(videoFrameRateImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(audioBitRateImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(audioSampleRateImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(stopOptionsImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(countDownImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(countDownTimeImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(cameraImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(floatingScreenShotImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(resolutionImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(shakeImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(excludeStatusImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(excludeNavigationImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(showPreviewDialogImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(brushImage, 40, 0, 0, 0)
        SRC_Helper.setMargin(textAndLogoImage, 40, 0, 0, 0)

        SRC_Helper.setMargin(r_arrow, 0, 0, 40, 0)
        SRC_Helper.setMargin(bit_arrow, 0, 0, 40, 0)
        SRC_Helper.setMargin(frame_arrow, 0, 0, 40, 0)
        SRC_Helper.setMargin(a_bit_arrow, 0, 0, 40, 0)
        SRC_Helper.setMargin(a_sample_arrow, 0, 0, 40, 0)
        SRC_Helper.setMargin(stop_arrow, 0, 0, 40, 0)
        SRC_Helper.setMargin(count_val_arrow, 0, 0, 40, 0)
    }

    private fun openStopOptionsActivity() {
        startActivity(
            Intent(
                activity,
                SRC_StopOptionsActivity::class.java
            )
        )
    }

    private fun openTextAndLogoActivity() {
        startActivity(
            Intent(
                activity,
                SRC_TextAndLogoActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        )
    }

    private fun openCountDownTimerDialog() {
        val dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.src_count_down_timer_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show()
        val getSeconds = dialog.findViewById<EditText>(R.id.getSeconds)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val ok = dialog.findViewById<TextView>(R.id.ok)

        SRC_Helper.setSize(dialog.timeLimit_Popup, 870, 574, true)
        SRC_Helper.setSize(dialog.line, 768, 4, true)
        SRC_Helper.setSize(dialog.editBg, 654, 134, true)

        SRC_Helper.setMargin(dialog.line, 0, 50, 0, 0)
        SRC_Helper.setMargin(dialog.editBg, 0, 50, 0, 0)

        getSeconds.setText(sharedPreferences.getString("countDownTimer", "3"))

        cancel.setOnClickListener { dialog.dismiss() }
        ok.setOnClickListener {
            if (getSeconds.text.toString().length != 0) {
                countDownTimeDetailTV.text = "${getSeconds.text}s"
                editor.putString("countDownTimer", getSeconds.text.toString())
                editor.apply()
                dialog.dismiss()
            } else {
                Toast.makeText(activity!!, "Please enter value", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initSwitches() {
//        enableFloating?.isChecked = sharedPreferences.getBoolean("enableFloating", true)
        enableScreenshotSound?.isChecked =
            sharedPreferences.getBoolean("enableScreenshotSound", false)
        enableShake?.isChecked = sharedPreferences.getBoolean("enableShake", false)
        enableExcludeStatus?.isChecked = sharedPreferences.getBoolean("enableExcludeStatus", false)
        enableExcludeNavigation?.isChecked =
            sharedPreferences.getBoolean("enableExcludeNavigation", false)
        enableShowPreviewDialog?.isChecked =
            sharedPreferences.getBoolean("enableShowPreviewDialog", true)
        enableCamera?.isChecked =
            sharedPreferences.getBoolean("enableCamera", false)
        enableBrush?.isChecked =
            sharedPreferences.getBoolean("enableBrush", false)

        if (sharedPreferences.getBoolean("enableCountDown", false)){
            enableCountDown?.isChecked = true
            countDownTimeLayout.visibility = View.VISIBLE
        }
        else{
            enableCountDown?.isChecked = false
            countDownTimeLayout.visibility = View.GONE
        }

        enableFloatingScreenShot?.isChecked =
            sharedPreferences.getBoolean("enableFloatingScreenShot", false)

        videoResolutionDetailTV.text = sharedPreferences.getString("Resolution", "1080p")
        videoBitRateDetailTV.text = sharedPreferences.getString("Video BitRate", "8200k Hz")
        videoFrameRateDetailTV.text = sharedPreferences.getString("Frame Rate", "60 FPS")
        audioBitRateDetailTV.text = sharedPreferences.getString("Audio BitRate", "160 kbps")
        audioSampleRateDetailTV.text = sharedPreferences.getString("Audio Sample Rate", "24000 Hz")
        countDownTimeDetailTV.text = "${sharedPreferences.getString("countDownTimer", "3")}s"

        var screenOff: String
        var shake: String
        var timeLimit: String

        if (sharedPreferences.getBoolean("enableStopOnScreenOff", false))
            screenOff = ", locking screen, "
        else
            screenOff = ""
        if (sharedPreferences.getBoolean("enableStopOnShake", false))
            shake = "shake, "
        else
            shake = ""
        if (sharedPreferences.getBoolean("enableTimeLimit", false))
            timeLimit = "timeLimit"
        else
            timeLimit = ""

        stopOptionsDetailTV.text = "Notification$screenOff$shake$timeLimit"

    }

    private fun getSwitchClick() {
        enableFloating.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 24)
            } else {
                activity?.stopService(Intent(activity, SRC_ScreenshotFloatingButtonService::class.java))
            }

            editor.putBoolean("enableFloating", isChecked)
            editor.apply()
        }
        enableScreenshotSound.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("enableScreenshotSound", isChecked)
            editor.apply()
        }
        enableShake.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                editor.putBoolean("enableStopOnShake", false)
            editor.putBoolean("enableShake", isChecked)
            editor.apply()
        }
        enableExcludeStatus.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("enableExcludeStatus", isChecked)
            editor.apply()
        }
        enableExcludeNavigation.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("enableExcludeNavigation", isChecked)
            editor.apply()
        }
        enableShowPreviewDialog.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("enableShowPreviewDialog", isChecked)
            editor.apply()
        }
        enableCamera.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                activity?.startService(Intent(activity, FloatingCameraViewService::class.java))
//            } else {
//                activity?.stopService(Intent(activity, FloatingCameraViewService::class.java))
//            }
            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("turn_off_floating_camera", isChecked)
            intent.putExtra("whichFloating", "camera")
            activity?.sendBroadcast(intent)
            editor.putBoolean("enableCamera", isChecked)
            editor.apply()
        }
        enableBrush.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("turn_on_off_brush", isChecked)
            intent.putExtra("whichFloating", "brush")
            activity?.sendBroadcast(intent)
            editor.putBoolean("enableBrush", isChecked)
            editor.apply()
        }
        enableFloatingScreenShot.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("turn_on_off_enable_floating_screenShot", isChecked)
            intent.putExtra("whichFloating", "screenShot")
            activity?.sendBroadcast(intent)
            editor.putBoolean("enableFloatingScreenShot", isChecked)
            editor.apply()
        }

        enableCountDown.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                countDownTimeLayout.visibility = View.VISIBLE
            else
                countDownTimeLayout.visibility = View.GONE
            editor.putBoolean("enableCountDown", isChecked)
            editor.apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 24) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(
                        Intent(activity, SRC_ScreenshotFloatingButtonService::class.java)
                            .putExtra(SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_CODE, requestCode)
                            .putExtra(SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_INTENT, data)
                    )
                } else {
                    activity?.startService(
                        Intent(activity, SRC_ScreenshotFloatingButtonService::class.java)
                            .putExtra(SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_CODE, requestCode)
                            .putExtra(SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_INTENT, data)
                    )
                }
            }
        } else {
            Log.d("Result", "onActivityResult: asdfghjkl;")
            enableFloating.isChecked = isMyServiceRunning(SRC_ScreenshotFloatingButtonService::class.java)
        }
    }

    var myReceiverIsRegistered: Boolean = false

    override fun onResume() {
        super.onResume()
        initSwitches()
        if (!myReceiverIsRegistered) {
            myReceiverIsRegistered = true
            activity?.registerReceiver(
                GetFloatingButtonBroadcastReceiver,
                IntentFilter("com.mycompany.myapp.SOME_MESSAGE")
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (myReceiverIsRegistered) {
            myReceiverIsRegistered = false
            activity?.unregisterReceiver(GetFloatingButtonBroadcastReceiver)
        }
    }


    fun onClickLayout() {
        videoResolutionLayout.setOnClickListener {
            configuration_popup(
                "Resolution",
                videoResolutionDetailTV,
                resources.getStringArray(R.array.video_resolutions)
            )
        }
        videoBitRateLayout.setOnClickListener {
            configuration_popup(
                "Video BitRate",
                videoBitRateDetailTV,
                resources.getStringArray(R.array.video_bitrates)
            )
        }
        videoFrameRateLayout.setOnClickListener {
            configuration_popup(
                "Frame Rate",
                videoFrameRateDetailTV,
                resources.getStringArray(R.array.video_framerates)
            )
        }
        audioBitRateLayout.setOnClickListener {
            configuration_popup(
                "Audio BitRate",
                audioBitRateDetailTV,
                resources.getStringArray(R.array.audio_bitrates)
            )
        }
        audioSampleRateLayout.setOnClickListener {
            configuration_popup(
                "Audio Sample Rate",
                audioSampleRateDetailTV,
                resources.getStringArray(R.array.audio_samplerates)
            )
        }
    }


    val GetFloatingButtonBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals("com.mycompany.myapp.SOME_MESSAGE")) {
                if (intent?.getBooleanExtra("turn_off_floating_button", true)!!) {
                    enableFloating.isChecked = false
                }
                if (intent?.getBooleanExtra("turn_off_camera", true)!!) {
                    enableCamera.isChecked = false
                }
            }
        }
    }

    public var configurationDialog: Dialog? = null

    fun configuration_popup(
        whichPopup: String,
        textView: TextView,
        getListOfConfiguration: Array<String>
    ) {
//        val configurationDialog = Dialog(activity!!)
        configurationDialog = Dialog(activity!!)
        configurationDialog?.setContentView(R.layout.src_configuration_popup)
        configurationDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent);
        configurationDialog?.show()

//        configurationDialog!!.getWindow()?.setGravity(Gravity.RIGHT);

//        Helper.setSize(configurationDialog?.popup_Bg_Square,352, 412,true)

        val title: TextView = configurationDialog?.findViewById<TextView>(R.id.title)!!
        title.setText(whichPopup)
        val itemRecyclerView: RecyclerView =
            configurationDialog?.findViewById<RecyclerView>(R.id.configurationRecyclerView)!!
        val configurationAdapter = ConfigurationAdapter(
            activity!!,
            getListOfConfiguration,
            object : ConfigurationAdapter.OnItemClickListener {
                override fun onItemClicked(pos: Int) {
                    textView.text = getListOfConfiguration[pos]
                    configurationDialog?.dismiss()
                    editor.putString(whichPopup,getListOfConfiguration[pos])
                    editor.apply()
                }
            })
        itemRecyclerView.adapter = configurationAdapter

    }

    class ConfigurationAdapter(
        context: Context,
        array: Array<String>,
        val onItemClickListener: OnItemClickListener
    ) : RecyclerView.Adapter<ConfigurationAdapter.MyViewHolder>() {

        val mContext: Context = context
        val getListOfConfiguration = array
//        val onItemClickListener : OnItemClickListener = mContext as OnItemClickListener

        open interface OnItemClickListener {
            fun onItemClicked(pos: Int)
        }

        class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val item: TextView = itemView.findViewById(R.id.item)
            val mainLayout: LinearLayout = itemView.findViewById(R.id.mainLayout)


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.src_configuration_popup_adapter_layout, parent, false)
            return MyViewHolder(view)
        }

        override fun getItemCount(): Int {
            return getListOfConfiguration.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

            SRC_Helper.setSize(holder.item, 244, 70, true)

            holder.item.setText(getListOfConfiguration[position])
            holder.mainLayout.setOnClickListener {
                onItemClickListener.onItemClicked(position)
//                textView.text = getListOfConfiguration[position]
//                configurationDialog.dismiss()
            }
        }
    }

}
