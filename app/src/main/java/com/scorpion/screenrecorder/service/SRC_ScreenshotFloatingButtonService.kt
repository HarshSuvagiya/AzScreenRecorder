package com.scorpion.screenrecorder.service

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.*
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
import android.content.res.Configuration
import android.graphics.*
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.divyanshu.colorseekbar.ColorSeekBar
import com.divyanshu.draw.widget.DrawView
import com.google.android.cameraview.CameraView
import com.googlecode.mp4parser.FileDataSourceImpl
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.activity.SRC_MainActivity
import com.scorpion.screenrecorder.activity.SRC_ScreenshotPreviewActivity
import com.scorpion.screenrecorder.activity.SRC_VideoViewActivity
import com.scorpion.screenrecorder.common.SRC_ImageTransmogrifier
import com.scorpion.screenrecorder.common.SRC_ShakeDetector
import com.scorpion.screenrecorder.muxer.SRC_AudioEncodeConfig
import com.scorpion.screenrecorder.muxer.SRC_ScreenRecorder
import com.scorpion.screenrecorder.muxer.SRC_ScreenRecorder.*
import com.scorpion.screenrecorder.muxer.SRC_Utils
import com.scorpion.screenrecorder.muxer.SRC_VideoEncodeConfig
import com.scorpion.screenrecorder.utils.SRC_FileUtils
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenshotDirPath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class SRC_ScreenshotFloatingButtonService : Service(), SRC_ShakeDetector.Listener, View.OnClickListener {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var vibrator: Vibrator? = null
    val heightWidthOfRemoveView: Int = 200
    val heightWidthOfFloatingView: Int = 120
    var codecNameVideo: String = ""
    var codecNameAudio: String = ""
    var height: Int = 0
    var width: Int = 0
    var currentPositionX: Int = 0
    var currentPositionY: Int = 0
    var screenRecordingImageView: ImageView? = null
    var screen_recording_start_stop_play_pause_layout_image_view: ImageView? = null
    var screen_recording_settings_image_view: ImageView? = null
    var screen_recording_home_intent_layout_image_view: ImageView? = null

    var isOtherFloatingVisible: Boolean = false

    var chronometer: Chronometer? = null
    lateinit var animZoomIn: Animation

    companion object {
        private const val TAG = "ScreenRecorderService"
        val EXTRA_RESULT_CODE: String = "resultCode"
        var EXTRA_RESULT_INTENT: String = "resultIntent"
    }

    var LAYOUT_FLAG: Int = 0
    lateinit var floatingView: View
    lateinit var brushFloatingView: View
    lateinit var screenRecordingToolbox: View
    lateinit var floatingTextViewLayout: View
    lateinit var floatingLogoLayout: View
    lateinit var screenRecordingFloatingView: View
    lateinit var screen_recording_start_stop_play_pause_layout: View
    lateinit var blackTrans: View
    lateinit var countDownTimerView: View
    lateinit var drawingView: View
    lateinit var countDownTimerTextView: TextView
    lateinit var screen_recording_home_intent_layout: View
    lateinit var screen_recording_settings_layout: View
    lateinit var removeFloatingView: View
    lateinit var manager: WindowManager
    lateinit var wmgr: WindowManager
    lateinit var params: WindowManager.LayoutParams
    lateinit var brushFloatingParams: WindowManager.LayoutParams
    lateinit var screenRecordingParams: WindowManager.LayoutParams
    lateinit var floatingTextViewParams: WindowManager.LayoutParams
    lateinit var floatingLogoParams: WindowManager.LayoutParams
    lateinit var screenRecordingPlayPauseParams: WindowManager.LayoutParams
    lateinit var screenRecordingSettingsParams: WindowManager.LayoutParams
    lateinit var screenRecordingHomeIntentParams: WindowManager.LayoutParams
    lateinit var removeFloatingParams: WindowManager.LayoutParams
    lateinit var draw_view: DrawView
    private var mediaProjection: MediaProjection? = null
    lateinit var mediaProjectionManager: MediaProjectionManager
    lateinit var virtualDisplay: VirtualDisplay
    lateinit var mHandler: Handler
    private val VIRTUAL_DISPLAY_FLAGS =
        DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
    private val handlerThread =
        HandlerThread(javaClass.simpleName, Process.THREAD_PRIORITY_BACKGROUND)
    var resultCode: Int = 0
    lateinit var resultDataRecording: Intent
    lateinit var it: SRC_ImageTransmogrifier
    private val beeper = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    var isScreenshotTaken: Boolean = false
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "channelId"
    private val description = "description"
    lateinit var background: LinearLayout
    var timeWhenStopped: Long? = 0
    lateinit var floatingTextView: TextView
    lateinit var floatingLogo: ImageView
    lateinit var snapshot: ImageView
    lateinit var changeColor: ImageView
    lateinit var undo: ImageView
    lateinit var redo: ImageView
    lateinit var delete: ImageView
    lateinit var close: ImageView
    lateinit var closeColorPicker: ImageView
    lateinit var colorPicker: ConstraintLayout
    lateinit var drawing_controls: ConstraintLayout
    lateinit var strokeSeekBar: SeekBar
    lateinit var color_seek_bar: ColorSeekBar
    var startTone : Boolean = true
    //    lateinit var sensorMgr : SensorManager
    var isRemoveFloatingAdded: Boolean = false
    private lateinit var mSensorManager: SensorManager
    private lateinit var mShakeDetector: SRC_ShakeDetector
    var isDrawingViewAdded: Boolean = false
    override fun onBind(intent: Intent?): IBinder? {
        throw IllegalStateException("Binding not supported. Go away.")
    }

    lateinit var screenShotSwitch: Switch
    lateinit var cameraSwitch: Switch
    lateinit var brushSwitch: Switch

    lateinit var contentView: RemoteViews

    override fun onCreate() {
        super.onCreate()
        setNotification()
        sharedPreferences = getSharedPreferences("myPref", 0)
        editor = sharedPreferences.edit()
        registerSensorListener()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mediaProjectionManager =
            applicationContext.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wmgr = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)
        mMediaProjectionCallback = MediaProjectionCallback()

    }

    private fun registerSensorListener() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mShakeDetector = SRC_ShakeDetector(this)
        // Set sensitivity to medium
        mShakeDetector.setSensitivity(SRC_ShakeDetector.SENSITIVITY_MEDIUM)
        mShakeDetector.start(mSensorManager)
    }

    private fun setNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, SRC_MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val startRecordingReceiver =
            PendingIntent.getBroadcast(this, 0, Intent("startRecording"), 134217728)
        val playPauseRecordingReceiver =
            PendingIntent.getBroadcast(this, 0, Intent("playPauseRecording"), 134217728)
        val settingsToolboxReceiver =
            PendingIntent.getBroadcast(this, 0, Intent("SETTINGS_RECORDING_RECEIVER"), 134217728)

        // RemoteViews are used to use the content of
        // some different layout apart from the current activity layout
        contentView = RemoteViews(packageName, R.layout.src_activity_after_notification)

        contentView.setOnClickPendingIntent(R.id.startRecording, startRecordingReceiver)
        contentView.setOnClickPendingIntent(R.id.homeIntent, playPauseRecordingReceiver)
        contentView.setOnClickPendingIntent(R.id.settings, settingsToolboxReceiver)


        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel =
                NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContent(contentView)
                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        this.resources,
//                        R.drawable.ic_launcher_background
//                    )
//                )
                .setContentIntent(pendingIntent)
        } else {

            builder = Notification.Builder(this)
                .setContent(contentView)
                .setSmallIcon(R.mipmap.ic_launcher)
//                .setLargeIcon(
//                    BitmapFactory.decodeResource(
//                        this.resources,
//                        R.drawable.ic_launcher_background
//                    )
//                )
                .setContentIntent(pendingIntent)
        }
//        notificationManager.notify(1234, builder.build())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1234, builder.build(),android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST)
        }
        else{
            startForeground(1234, builder.build())
        }
//        startForeground(1234, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
//        updateNoti()
    }

    fun updateNoti() {
        notificationManager.notify(1234, builder.build())
//        startForeground(1234, builder.build())
    }

    fun getWindowManager(): WindowManager {
        return wmgr
    }

    fun getHandler(): Handler {
        return mHandler
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == null) {
            resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 1337)!!
            resultDataRecording = intent.getParcelableExtra<Intent>(EXTRA_RESULT_INTENT)!!
        }

        registerReceiver()
        registerReceiver(START_RECORDING_RECEIVER, IntentFilter("startRecording"))
        registerReceiver(PLAY_PAUSE_RECORDING_RECEIVER, IntentFilter("playPauseRecording"))
        registerReceiver(SETTINGS_TOOLBOX_RECEIVER, IntentFilter("SETTINGS_RECORDING_RECEIVER"))

        addRemoveFloatingView()
        addBlackTransparentView()
        addFloatingViewScreenshot()
        addScreenRecordingFloatingView()
        addFloatingBrush()
        addDrawingView()
        addFloatingCameraView()
        addFloatingTextView()
        addFloatingLogo()
        addPlayPauseScreenRecordingView()
        addSettingsScreenRecordingView()
        addHomeIntentScreenRecordingView()
        addScreenRecordingToolboxView()
        addCountDownTimerView()

        floatingView.findViewById<View>(R.id.capture)
            .setOnTouchListener(floatingButtonTouchListener)
        screenRecordingFloatingView.setOnTouchListener(screenRecordingFloatingButtonTouchListener)
        brushFloatingView.setOnTouchListener(floatingBrushTouchListener)
        floatingTextViewLayout.setOnTouchListener(textViewFloatingButtonTouchListener)
        floatingLogoLayout.setOnTouchListener(logoFloatingButtonTouchListener)

        getCodecForAPILevel24Below()

        return START_STICKY
    }

    private fun addFloatingLogo() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        floatingLogoParams = params
        params.gravity = Gravity.CENTER

        floatingLogoLayout = LayoutInflater.from(this).inflate(R.layout.src_floating_logo_layout, null)
        floatingLogo = floatingLogoLayout.findViewById(R.id.floatingLogo)
        manager.addView(floatingLogoLayout, params)
        floatingLogoLayout.visibility = View.GONE
    }

    private fun addFloatingTextView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        floatingTextViewParams = params
        params.gravity = Gravity.CENTER

        floatingTextViewLayout =
            LayoutInflater.from(this).inflate(R.layout.src_floating_text_layout, null)
        floatingTextView = floatingTextViewLayout.findViewById(R.id.floatingTextView)
        manager.addView(floatingTextViewLayout, params)
        floatingTextViewLayout.visibility = View.GONE

    }

    private fun addScreenRecordingToolboxView() {

        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 750 / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 700 / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        params.gravity = Gravity.CENTER

        screenRecordingToolbox = LayoutInflater.from(this).inflate(R.layout.src_toolbox_layout, null)
        manager.addView(screenRecordingToolbox, params)
        screenRecordingToolbox.visibility = View.GONE

        val closeToolbox = screenRecordingToolbox.findViewById<ImageView>(R.id.closeToolbox)
        screenShotSwitch = screenRecordingToolbox.findViewById<Switch>(R.id.screenShotSwitch)
        cameraSwitch = screenRecordingToolbox.findViewById<Switch>(R.id.cameraSwitch)
        brushSwitch = screenRecordingToolbox.findViewById<Switch>(R.id.brushSwitch)

        screenShotSwitch.isChecked = sharedPreferences.getBoolean("enableFloatingScreenShot", false)
        cameraSwitch.isChecked = sharedPreferences.getBoolean("enableCamera", false)
        brushSwitch.isChecked = sharedPreferences.getBoolean("enableBrush", false)

        closeToolbox.setOnClickListener {
            screenRecordingToolbox.visibility = View.GONE
        }

        screenShotSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) visibleView(floatingView) else hideView(floatingView)
            editor.putBoolean("enableFloatingScreenShot", isChecked)
            editor.apply()
        }
        cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) mFloatingCameraView?.visibility =
                View.VISIBLE else mFloatingCameraView?.visibility = View.GONE
            editor.putBoolean("enableCamera", isChecked)
            editor.apply()
        }
        brushSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) visibleView(brushFloatingView) else hideView(brushFloatingView)
            editor.putBoolean("enableBrush", isChecked)
            editor.apply()
        }

    }

    private fun addFloatingBrush() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        this.brushFloatingParams = params
        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        params.x = 0
        params.y = (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() / 3) * 2

        brushFloatingView = LayoutInflater.from(this).inflate(R.layout.src_brush_floating_layout, null)
        manager.addView(brushFloatingView, params)
        brushFloatingView.visibility = View.GONE
        fadeWidget(brushFloatingView, params)
    }

    private fun addDrawingView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        drawingView =
            LayoutInflater.from(this)
                .inflate(R.layout.src_drawing_layout, null)
        manager.addView(drawingView, screenRecordingParams)
        draw_view = drawingView.findViewById(R.id.draw_view)
        drawingView.visibility = View.GONE
        snapshot = drawingView.findViewById<ImageView>(R.id.snapshot)
        changeColor = drawingView.findViewById<ImageView>(R.id.changeColor)
        undo = drawingView.findViewById<ImageView>(R.id.undo)
        redo = drawingView.findViewById<ImageView>(R.id.redo)
        delete = drawingView.findViewById<ImageView>(R.id.delete)
        close = drawingView.findViewById<ImageView>(R.id.close)
        closeColorPicker = drawingView.findViewById<ImageView>(R.id.closeColorPicker)
        colorPicker = drawingView.findViewById<ConstraintLayout>(R.id.colorPicker)
        drawing_controls = drawingView.findViewById<ConstraintLayout>(R.id.drawing_controls)
        strokeSeekBar = drawingView.findViewById<SeekBar>(R.id.strokeSeekBar)
        color_seek_bar = drawingView.findViewById<ColorSeekBar>(R.id.color_seek_bar)

    }

    private fun addCountDownTimerView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth(),
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight(),
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        countDownTimerView =
            LayoutInflater.from(this)
                .inflate(R.layout.src_count_down_window_layout, null)
        manager.addView(countDownTimerView, screenRecordingParams)
        countDownTimerView.visibility = View.GONE
        countDownTimerTextView = countDownTimerView.findViewById(R.id.countDownTimerTextView)
        animZoomIn = AnimationUtils.loadAnimation(applicationContext, R.anim.zoom_in)

    }

    private fun addBlackTransparentView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth(),
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight(),
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        blackTrans =
            LayoutInflater.from(this)
                .inflate(R.layout.src_black_transparent_layout, null)
        manager.addView(blackTrans, screenRecordingParams)
        blackTrans.visibility = View.GONE
        blackTrans.setOnClickListener {
            hideOtherFloatingView()
            blackTrans.visibility = View.GONE
        }
    }

    private fun addHomeIntentScreenRecordingView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

//        this.screenRecordingHomeIntentParams = screenRecordingParams
        //Specify the view position
        screenRecordingParams.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        screenRecordingParams.x = this.screenRecordingParams.x - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 150 / 1080
        screenRecordingParams.y = this.screenRecordingParams.y

        this.screenRecordingHomeIntentParams = screenRecordingParams

        screen_recording_home_intent_layout =
            LayoutInflater.from(this)
                .inflate(R.layout.src_screen_recording_home_intent_layout, null)
        screen_recording_home_intent_layout_image_view =
            screen_recording_home_intent_layout.findViewById(R.id.homeIntent)
        manager.addView(screen_recording_home_intent_layout, screenRecordingParams)
        screen_recording_home_intent_layout.visibility = View.GONE

        screen_recording_home_intent_layout.setOnClickListener {
            startActivity(
                Intent(applicationContext, SRC_MainActivity::class.java)
                    .setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    .setFlags(FLAG_ACTIVITY_NEW_TASK)
            )
            hideOtherFloatingView()
        }

    }

    private fun addSettingsScreenRecordingView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

//        this.screenRecordingSettingsParams = screenRecordingParams
        //Specify the view position
        screenRecordingParams.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        screenRecordingParams.x = this.screenRecordingParams.x - 175
        screenRecordingParams.y = this.screenRecordingParams.y + 175

        this.screenRecordingSettingsParams = screenRecordingParams

        screen_recording_settings_layout =
            LayoutInflater.from(this)
                .inflate(R.layout.src_screen_recording_settingslayout, null)
        screen_recording_settings_image_view =
            screen_recording_settings_layout.findViewById(R.id.settings)
        manager.addView(screen_recording_settings_layout, screenRecordingParams)
        screen_recording_settings_layout.visibility = View.GONE

        screen_recording_settings_layout.setOnClickListener {
            if (mIsRecording)
                pauseResumeScreenRecording()
            else {
                screenShotSwitch.isChecked =
                    sharedPreferences.getBoolean("enableFloatingScreenShot", false)
                cameraSwitch.isChecked = sharedPreferences.getBoolean("enableCamera", false)
                brushSwitch.isChecked = sharedPreferences.getBoolean("enableBrush", false)
                screenRecordingToolbox.visibility = View.VISIBLE
                hideOtherFloatingView()
                hideView(blackTrans)
            }
        }

    }

    private fun pauseResumeScreenRecording() {
        hideOtherFloatingView()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (mIsRecording) {
                if (!mIsRecordingPause) {
                    mIsRecordingPause = true
                    mMediaRecorder?.pause()
                    timeWhenStopped = chronometer?.base!! - SystemClock.elapsedRealtime()
                    chronometer?.stop()
                    screen_recording_settings_image_view?.setImageResource(R.drawable.src_f_play_button)
                    contentView.setImageViewResource(
                        R.id.homeIntent,
                        R.drawable.src_n_play_button
                    )
                    updateNoti()
                } else {
                    mIsRecordingPause = false
                    mMediaRecorder?.resume()
                    chronometer?.base = SystemClock.elapsedRealtime() + timeWhenStopped!!
                    chronometer?.start()
                    screen_recording_settings_image_view?.setImageResource(R.drawable.src_f_pause_button)
                    contentView.setImageViewResource(
                        R.id.homeIntent,
                        R.drawable.src_n_pause_button
                    )
                    updateNoti()
                    timeWhenStopped = 0
                }
            }
        } else {
            if (mIsRecording) {
                if (!mIsRecordingPause) {
                    mIsRecordingPause = true
                    isPauseForAPI24LevelBelow = true
                    mLastPausedTimeUs = System.nanoTime() / 1000
                    timeWhenStopped = chronometer?.base!! - SystemClock.elapsedRealtime()
                    chronometer?.stop()
                    screen_recording_settings_image_view?.setImageResource(R.drawable.src_n_play_button)
                    contentView.setImageViewResource(
                        R.id.homeIntent,
                        R.drawable.src_n_play_button
                    )
                    updateNoti()
                } else {
                    mIsRecordingPause = false
                    if (mLastPausedTimeUs != 0L) {
                        offsetPTSUs += System.nanoTime() / 1000 - mLastPausedTimeUs
                        mLastPausedTimeUs = 0
                    }
                    isPauseForAPI24LevelBelow = false
                    chronometer?.base = SystemClock.elapsedRealtime() + timeWhenStopped!!
                    chronometer?.start()
                    screen_recording_settings_image_view?.setImageResource(R.drawable.src_n_pause_button)
                    contentView.setImageViewResource(
                        R.id.homeIntent,
                        R.drawable.src_n_pause_button
                    )
                    updateNoti()
                    timeWhenStopped = 0
                }
            }
        }
    }

    private fun stopScreenRecordingForLowerThanN() {
        baseStopRecording()
        hideOtherFloatingView()
        mIsRecording = true
    }

    private fun addPlayPauseScreenRecordingView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

//        this.screenRecordingPlayPauseParams = screenRecordingParams
        //Specify the view position
        screenRecordingParams.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        screenRecordingParams.x = this.screenRecordingParams.x - 175
        screenRecordingParams.y = this.screenRecordingParams.y - 175

        this.screenRecordingPlayPauseParams = screenRecordingParams

        screen_recording_start_stop_play_pause_layout =
            LayoutInflater.from(this)
                .inflate(R.layout.src_screen_recording_start_stop_play_pause_layout, null)
        screen_recording_start_stop_play_pause_layout_image_view =
            screen_recording_start_stop_play_pause_layout.findViewById(R.id.actionForScreenRecording)
        manager.addView(screen_recording_start_stop_play_pause_layout, screenRecordingParams)
        screen_recording_start_stop_play_pause_layout.visibility = View.GONE

        screen_recording_start_stop_play_pause_layout.setOnClickListener {
            showCountDownBeforeStartingRecording()
        }
    }

    fun showCountDownBeforeStartingRecording() {
        if (!mIsRecording) {
            if (sharedPreferences.getBoolean("enableCountDown", false)) {
                hideOtherFloatingView()
                countDownTimerView.visibility = View.VISIBLE
                var time: String = sharedPreferences.getString("countDownTimer", "3")!!
                object : CountDownTimer((time.toLong() * 1000), 1000) {
                    override fun onFinish() {
                        countDownTimerView.visibility = View.GONE
                        startRecordingInGeneral()
//                        startRecording(resultDataRecording)
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        time = (time.toInt() - 1).toString()
                        countDownTimerTextView.text = (time.toInt() + 1).toString()
                        countDownTimerTextView.startAnimation(animZoomIn)
                    }

                }.start()
            } else {
                startRecordingInGeneral()
//                startRecording(resultDataRecording)
            }
        } else {
            stopRecordingInGeneral()
//            stopRecording()
        }
    }

    val floatingButtonTouchListener: View.OnTouchListener = object : View.OnTouchListener {

        var initialX: Int? = null
        var initialY: Int? = null
        var initialTouchX: Float? = null
        var initialTouchY: Float? = null

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    floatingView!!.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateRemoveView(MotionEvent.ACTION_DOWN)
                    fadeHandler.removeCallbacksAndMessages(null)
                    floatingView.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
                    initialX = params.x
                    initialY = params.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    isRemoveFloatingAdded = false
                    fadeWidget(floatingView, params)

                    val Xdiff = (event.getRawX() - initialTouchX!!)
                    val Ydiff = (event.getRawY() - initialTouchY!!)

                    removeFloatingView.visibility = View.GONE
                    if (Math.abs(Xdiff) < 5 && Math.abs(Ydiff) < 5) {
                        takeScreenshot()
                        return true
                    }

                    hideFloatingView(floatingView, true, "floatingScreenShot")
                    updatePosition(floatingView, params)

                    return true
                }

                MotionEvent.ACTION_MOVE -> {

                    visibleView(removeFloatingView)

                    //remove floating view
                    hideFloatingView(floatingView, false, "floatingScreenShot")

                    val locationFloating = IntArray(2)
                    floatingView.getLocationOnScreen(locationFloating)
                    val x1 = locationFloating[0]
                    val y1 = locationFloating[1]

                    val floatingViewTopLeft = Point(x1, y1)

                    Log.d("pos123", event.rawX.toString())
                    Log.d("pos123", (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() / 2).toString())

                    params.x = initialX!!.plus(event.rawX - initialTouchX!!).toInt()
                    params.y = initialY!!.plus(event.rawY - initialTouchY!!).toInt()
                    manager.updateViewLayout(floatingView, params)
                    return true
                }
            }

            return false
        }
    }

    val floatingBrushTouchListener: View.OnTouchListener = object : View.OnTouchListener {

        var initialX: Int? = null
        var initialY: Int? = null
        var initialTouchX: Float? = null
        var initialTouchY: Float? = null

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    floatingView!!.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateRemoveView(MotionEvent.ACTION_DOWN)
                    fadeHandler.removeCallbacksAndMessages(null)
                    brushFloatingView.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
                    initialX = brushFloatingParams.x
                    initialY = brushFloatingParams.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    isRemoveFloatingAdded = false
                    fadeWidget(brushFloatingView, brushFloatingParams)

                    val Xdiff = (event.getRawX() - initialTouchX!!)
                    val Ydiff = (event.getRawY() - initialTouchY!!)

                    removeFloatingView.visibility = View.GONE
                    if (Math.abs(Xdiff) < 5 && Math.abs(Ydiff) < 5) {
                        startDrawing()
                        return true
                    }

                    hideFloatingView(brushFloatingView, true, "floatingBrush")
                    updatePosition(brushFloatingView, brushFloatingParams)

                    return true
                }

                MotionEvent.ACTION_MOVE -> {

                    visibleView(removeFloatingView)

                    //remove floating view
                    hideFloatingView(brushFloatingView, false, "floatingBrush")

                    brushFloatingParams.x = initialX!!.plus(event.rawX - initialTouchX!!).toInt()
                    brushFloatingParams.y = initialY!!.plus(event.rawY - initialTouchY!!).toInt()
                    manager.updateViewLayout(brushFloatingView, brushFloatingParams)
                    return true
                }
            }

            return false
        }
    }

    val screenRecordingFloatingButtonTouchListener: View.OnTouchListener =
        object : View.OnTouchListener {

            var initialX: Int? = null
            var initialY: Int? = null
            var initialTouchX: Float? = null
            var initialTouchY: Float? = null

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    screenRecordingFloatingView!!.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
                when (event!!.action) {
                    MotionEvent.ACTION_DOWN -> {
                        animateRemoveView(MotionEvent.ACTION_DOWN)
                        fadeHandler.removeCallbacksAndMessages(null)
                        screenRecordingFloatingView.animate().scaleX(1f).scaleY(1f).alpha(1f)
                            .translationX(0f)
                        initialX = screenRecordingParams.x
                        initialY = screenRecordingParams.y

                        initialTouchX = event.rawX
                        initialTouchY = event.rawY

                        return true
                    }

                    MotionEvent.ACTION_UP -> {

                        isRemoveFloatingAdded = false

                        val Xdiff = (event.getRawX() - initialTouchX!!)
                        val Ydiff = (event.getRawY() - initialTouchY!!)

                        removeFloatingView.visibility = View.GONE
                        if (Math.abs(Xdiff) < 5 && Math.abs(Ydiff) < 5) {
//                            if (!isOtherFloatingVisible)
                            makeOtherFloatingVisible(isOtherFloatingVisible)
                            return true
                        }
                        fadeWidget(screenRecordingFloatingView, screenRecordingParams)

                        if (!isOtherFloatingVisible) {

                            screenRecordingParams.x =
                                initialX!! + (event.rawX - initialTouchX!!).toInt()
                            screenRecordingParams.y =
                                initialY!! + (event.rawY - initialTouchY!!).toInt()

//                            finalScreenRecordingParams = screenRecordingParams

                            currentPositionX = screenRecordingParams.x
                            currentPositionY = screenRecordingParams.y
                            hideFloatingView(screenRecordingFloatingView, true, "")
                            updatePosition(screenRecordingFloatingView, screenRecordingParams)
                        }

                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {

                        if (!isOtherFloatingVisible) {
                            visibleView(removeFloatingView)

                            //remove floating view
                            hideFloatingView(screenRecordingFloatingView, false, "")

                            val locationFloating = IntArray(2)
                            screenRecordingFloatingView.getLocationOnScreen(locationFloating)

                            screenRecordingParams.x =
                                initialX!!.plus(event.rawX - initialTouchX!!).toInt()
                            screenRecordingParams.y =
                                initialY!!.plus(event.rawY - initialTouchY!!).toInt()

                            Log.d("screenRecordingParamsX1", screenRecordingParams.x.toString())
                            manager.updateViewLayout(
                                screenRecordingFloatingView,
                                screenRecordingParams
                            )
                        }

                        return true
                    }
                }

                return false
            }
        }

    val textViewFloatingButtonTouchListener: View.OnTouchListener = object : View.OnTouchListener {

        var initialX: Int? = null
        var initialY: Int? = null
        var initialTouchX: Float? = null
        var initialTouchY: Float? = null

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    screenRecordingFloatingView!!.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateRemoveView(MotionEvent.ACTION_DOWN)
                    fadeHandler.removeCallbacksAndMessages(null)
                    initialX = floatingTextViewParams.x
                    initialY = floatingTextViewParams.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    return true
                }

                MotionEvent.ACTION_UP -> {

                    return true
                }

                MotionEvent.ACTION_MOVE -> {

                    floatingTextViewParams.x =
                        initialX!!.plus(event.rawX - initialTouchX!!).toInt()
                    floatingTextViewParams.y =
                        initialY!!.plus(event.rawY - initialTouchY!!).toInt()


                    manager.updateViewLayout(
                        floatingTextViewLayout,
                        floatingTextViewParams
                    )

                    return true
                }
            }

            return false
        }
    }

    val logoFloatingButtonTouchListener: View.OnTouchListener = object : View.OnTouchListener {

        var initialX: Int? = null
        var initialY: Int? = null
        var initialTouchX: Float? = null
        var initialTouchY: Float? = null

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                    screenRecordingFloatingView!!.animate().scaleX(1f).scaleY(1f).alpha(1f).translationX(0f)
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    animateRemoveView(MotionEvent.ACTION_DOWN)
                    fadeHandler.removeCallbacksAndMessages(null)
                    initialX = floatingLogoParams.x
                    initialY = floatingLogoParams.y

                    initialTouchX = event.rawX
                    initialTouchY = event.rawY

                    return true
                }

                MotionEvent.ACTION_UP -> {

                    return true
                }

                MotionEvent.ACTION_MOVE -> {

                    floatingLogoParams.x =
                        initialX!!.plus(event.rawX - initialTouchX!!).toInt()
                    floatingLogoParams.y =
                        initialY!!.plus(event.rawY - initialTouchY!!).toInt()


                    manager.updateViewLayout(
                        floatingLogoLayout,
                        floatingLogoParams
                    )

                    return true
                }
            }
            return false
        }
    }

    fun startRecordingInGeneral() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startRecording(resultDataRecording)
        } else {
            startRecordingForAPILevel24Below()
        }
    }

    private fun startDrawing() {
        drawingView.visibility = View.VISIBLE
        drawing_controls.visibility = View.VISIBLE
        isDrawingViewAdded = true
        close.setOnClickListener {
            isDrawingViewAdded = false
            drawingView.visibility = View.GONE
        }

        delete.setOnClickListener {
            draw_view.clearCanvas()
        }
        redo.setOnClickListener {
            draw_view.redo()
        }
        undo.setOnClickListener {
            draw_view.undo()
        }
        changeColor.setOnClickListener {
            colorPicker.visibility = View.VISIBLE
        }
        closeColorPicker.setOnClickListener {
            colorPicker.visibility = View.GONE
        }
        snapshot.setOnClickListener {
            takeScreenshot()
        }
        strokeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draw_view.setStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        color_seek_bar.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(color: Int) {
                draw_view.setColor(color)
            }
        })

    }

    fun animSmallGone(view: View) {
        view.animate().scaleX(0f).scaleY(0f).alpha(0f).rotation(-360f).duration = 300
    }

    fun animBigVisible(view: View) {
        view.animate().scaleX(1f).scaleY(1f).alpha(1f).rotation(360f).duration = 300
    }

    private fun makeOtherFloatingVisible(boolean: Boolean) {
        if (!boolean) {
            screen_recording_home_intent_layout.visibility = View.VISIBLE
            screen_recording_settings_layout.visibility = View.VISIBLE
            screen_recording_start_stop_play_pause_layout.visibility = View.VISIBLE
            blackTrans.visibility = View.VISIBLE

            animBigVisible(screen_recording_home_intent_layout)
            animBigVisible(screen_recording_settings_layout)
            animBigVisible(screen_recording_start_stop_play_pause_layout)
            updateOtherFloatingPosition()
        } else {
            blackTrans.visibility = View.GONE

            animSmallGone(screen_recording_home_intent_layout)
            animSmallGone(screen_recording_settings_layout)
            animSmallGone(screen_recording_start_stop_play_pause_layout)

            Handler().postDelayed({
                screen_recording_home_intent_layout.visibility = View.GONE
                screen_recording_settings_layout.visibility = View.GONE
                screen_recording_start_stop_play_pause_layout.visibility = View.GONE
            }, 300)

        }
        isOtherFloatingVisible = !isOtherFloatingVisible
    }

    private fun playEnterAnimation(view: View) {
        val ANIMATION_DURATION = 300

        /**
         * Animator that animates buttons x and y position simultaneously with size
         */
        val buttonAnimator = AnimatorSet()

        /**
         * ValueAnimator to update x position of a button
         */
        /**
         * ValueAnimator to update x position of a button
         */
        val buttonAnimatorX = ValueAnimator.ofFloat(
            this.screenRecordingPlayPauseParams.x + this.screenRecordingPlayPauseParams.x + view.getLayoutParams().width / 2.toFloat(),
            (this.screenRecordingPlayPauseParams.x + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 175 / 1080).toFloat()
        )
        buttonAnimatorX.addUpdateListener { animation ->
            view.x = animation.animatedValue as Float - view.layoutParams.width / 2
            view.requestLayout()
        }
        buttonAnimatorX.duration = ANIMATION_DURATION.toLong()
        /**
         * ValueAnimator to update y position of a button
         */
//        val buttonAnimatorY = ValueAnimator.ofFloat(
//            this.screenRecordingParams.y + 5.toFloat(),
//            (this.screenRecordingParams.y - Utils.getScreenHeight() * 175 / 1920).toFloat()
//        )
//        buttonAnimatorY.addUpdateListener { animation ->
//            view.y = animation.animatedValue as Float
//            view.requestLayout()
//        }
//        buttonAnimatorY.duration = ANIMATION_DURATION.toLong()
        /**
         * This will increase the size of button
         */
//        val buttonSizeAnimator = ValueAnimator.ofInt(5, width)
//        buttonSizeAnimator.addUpdateListener { animation ->
//            view.layoutParams.width = animation.animatedValue as Int
//            view.layoutParams.height = animation.animatedValue as Int
//            view.requestLayout()
//        }
//        buttonSizeAnimator.duration = ANIMATION_DURATION.toLong()
        /**
         * Add both x and y position update animation in
         * animator set
         */
//        buttonAnimator.play(buttonAnimatorX).with(buttonAnimatorY).with(buttonSizeAnimator)
        buttonAnimator.play(buttonAnimatorX)
        buttonAnimator.startDelay = 80
        buttonAnimator.start()
    }

    private fun updateOtherFloatingPosition() {

        Log.d("paramsx", this.screenRecordingParams.x.toString())

        if (this.screenRecordingParams.x > com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() / 2) {
            screenRecordingSettingsParams.x =
                this.screenRecordingParams.x - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 175 / 1080
            screenRecordingSettingsParams.y =
                this.screenRecordingParams.y + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 175 / 1920
            screenRecordingHomeIntentParams.x =
                this.screenRecordingParams.x - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 300 / 1080
            screenRecordingHomeIntentParams.y = this.screenRecordingParams.y
            screenRecordingPlayPauseParams.x =
                this.screenRecordingParams.x - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 175 / 1080
            screenRecordingPlayPauseParams.y =
                this.screenRecordingParams.y - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 175 / 1920
            manager.updateViewLayout(
                screen_recording_settings_layout,
                screenRecordingSettingsParams
            )
            manager.updateViewLayout(
                screen_recording_home_intent_layout,
                screenRecordingHomeIntentParams
            )
            manager.updateViewLayout(
                screen_recording_start_stop_play_pause_layout,
                screenRecordingPlayPauseParams
            )
        } else {
            screenRecordingSettingsParams.x =
                this.screenRecordingParams.x + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 75 / 1080
            screenRecordingSettingsParams.y =
                this.screenRecordingParams.y + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 175 / 1920
            screenRecordingHomeIntentParams.x =
                this.screenRecordingParams.x + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 200 / 1080
            screenRecordingHomeIntentParams.y = this.screenRecordingParams.y
            screenRecordingPlayPauseParams.x =
                this.screenRecordingParams.x + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 75 / 1080
            screenRecordingPlayPauseParams.y =
                this.screenRecordingParams.y - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 175 / 1920
            manager.updateViewLayout(
                screen_recording_settings_layout,
                screenRecordingSettingsParams
            )
            manager.updateViewLayout(
                screen_recording_home_intent_layout,
                screenRecordingHomeIntentParams
            )
            manager.updateViewLayout(
                screen_recording_start_stop_play_pause_layout,
                screenRecordingPlayPauseParams
            )
        }
    }

    private fun visibleView(view: View) {
        view.visibility = View.VISIBLE
    }

    private fun hideView(view: View) {
        view.visibility = View.GONE
    }

    private fun addScreenRecordingFloatingView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val screenRecordingParams = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        screenRecordingParams.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        screenRecordingParams.x = com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth()
        screenRecordingParams.y = com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() / 2

        this.screenRecordingParams = screenRecordingParams

        screenRecordingFloatingView =
            LayoutInflater.from(this).inflate(R.layout.src_screen_recording_floating_layout, null)
        screenRecordingImageView = screenRecordingFloatingView.findViewById(R.id.screenRecording)
        chronometer = screenRecordingFloatingView.findViewById(R.id.chronometer)
        manager.addView(screenRecordingFloatingView, screenRecordingParams)
        fadeWidget(screenRecordingFloatingView, screenRecordingParams)
    }

    private fun showPreviewPopup(path: String) {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            720,
            1080,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        params.gravity = Gravity.CENTER

//        showPreviewPopup = LayoutInflater.from(this).inflate(R.layout.show_preview_popup, null)
//        popupImage = showPreviewPopup.findViewById(R.id.popImage)
//        manager.addView(showPreviewPopup, params)

    }

    private fun hideFloatingView(view: View, shouldRemoveView: Boolean, name: String) {
        val location = IntArray(2)
        removeFloatingView.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]

        val removeFloatingViewTopLeft = Point(x, y)
        val removeFloatingViewTopRight =
            Point(x + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfRemoveView / 1080), y)
        val removeFloatingViewBottomLeft =
            Point(x, y + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfRemoveView / 1080))
        val removeFloatingViewBottomRight = Point(
            x + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfRemoveView / 1080),
            y + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfRemoveView / 1080)
        )

        val locationFloating = IntArray(2)
        view.getLocationOnScreen(locationFloating)
        val x1 = locationFloating[0]
        val y1 = locationFloating[1]

        val floatingViewTopLeft = Point(x1, y1)
        val floatingViewTopRight =
            Point(x1 + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080), y1)
        val floatingViewBottomLeft =
            Point(x1, y1 + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1080))
        val floatingViewBottomRight = Point(
            x1 + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080),
            y1 + (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1080)
        )

        if (floatingViewTopLeft.x > removeFloatingViewTopLeft.x && floatingViewTopLeft.y > removeFloatingViewTopLeft.y &&
            floatingViewTopRight.x < removeFloatingViewTopRight.x && floatingViewTopRight.y > removeFloatingViewTopRight.y &&
            floatingViewBottomLeft.x > removeFloatingViewBottomLeft.x && floatingViewBottomLeft.y < removeFloatingViewBottomLeft.y &&
            floatingViewBottomRight.x < removeFloatingViewBottomRight.x && floatingViewBottomRight.y < removeFloatingViewBottomRight.y
        ) {
//            background.setBackgroundColor(applicationContext.resources.getColor(R.color.red))

            if (shouldRemoveView) {

                if (name.equals("floatingScreenShot")) {
                    editor.putBoolean("enableFloatingScreenShot", false)
                    editor.apply()
                }
                if (name.equals("floatingBrush")) {
                    editor.putBoolean("enableBrush", false)
                    editor.apply()
                }
                hideView(view)
                hideView(removeFloatingView)

//                editor.putBoolean("enableFloating", false)
//                editor.apply()

                // turn off switch when settingsFrag is open
//                val intent = Intent("com.mycompany.myapp.SOME_MESSAGE")
//                intent.putExtra("turn_off_floating_button", true)
//                sendBroadcast(intent)
            }


        } else {
//            background.setBackgroundColor(applicationContext.resources.getColor(R.color.black_trans))
        }

    }

    private fun addFloatingViewScreenshot() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * heightWidthOfFloatingView / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * heightWidthOfFloatingView / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        this.params = params
        //Specify the view position
        params.gravity =
            Gravity.TOP or Gravity.LEFT //Initially view will be added to top-left corner
        params.x = 0
        params.y = com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() / 3

        floatingView = LayoutInflater.from(this).inflate(R.layout.src_screenshot_floating_layout, null)
        manager.addView(floatingView, params)
        hideView(floatingView)
        fadeWidget(floatingView, params)
    }

    private fun addRemoveFloatingView() {
        LAYOUT_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        val params = WindowManager.LayoutParams(
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 200 / 1080,
            com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 200 / 1920,
            LAYOUT_FLAG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        this.removeFloatingParams = params
        //Specify the view position
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.x = 0
        params.y = com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() / 5
        removeFloatingView =
            LayoutInflater.from(this).inflate(R.layout.src_remove_floating_layout, null)
        background = removeFloatingView.findViewById(R.id.background)
        manager.addView(removeFloatingView, params)
        hideView(removeFloatingView)
    }

    private fun takeScreenshot() {
        hideOtherFloatingView()
        hideView(floatingView)
        hideView(brushFloatingView)
        hideView(screenRecordingFloatingView)
        hideView(drawing_controls)

        isScreenshotTaken = true
        startTone = true
        if (!mIsRecording) {
            try {
                mediaProjection = mediaProjectionManager.getMediaProjection(
                    Activity.RESULT_OK,
                    resultDataRecording
                )
            } catch (e: java.lang.Exception) {
            }
        }
        it = SRC_ImageTransmogrifier(this)

        val cb: MediaProjection.Callback = object : MediaProjection.Callback() {
            override fun onStop() {
                virtualDisplay.release()
            }
        }

        Handler().postDelayed({
            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "screenshot",
                it.getWidth(),
                it.getHeight(),
                resources.displayMetrics.densityDpi,
                VIRTUAL_DISPLAY_FLAGS,
                it.getSurface(),
                null,
                mHandler
            )

            mediaProjection!!.registerCallback(cb, mHandler)
        }, 100)


        Handler().postDelayed({
            visibleView(screenRecordingFloatingView)
            if (sharedPreferences.getBoolean("enableBrush", false))
                visibleView(brushFloatingView)
            if (sharedPreferences.getBoolean("enableFloatingScreenShot", false))
                visibleView(floatingView)
            if (isDrawingViewAdded)
                visibleView(drawing_controls)
        }, 1000)

    }

    private var fadeHandler = Handler()

    private fun fadeWidget(view: View, params: WindowManager.LayoutParams) {
        fadeHandler.postDelayed({
            if (params != null && view != null) {
                if (params.x > (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() / 2))
                    view.animate().scaleX(0.75f).scaleY(0.75f).alpha(0.5f).translationX(30f)
                        .setDuration(300)
                else
                    view.animate().scaleX(0.75f).scaleY(0.75f).alpha(0.5f).translationX(-30f)
                        .setDuration(300)
            }
        }, 2000)
    }

    private fun removeView(view: View) {
        if (view.getWindowToken() != null) {
            manager.removeView(view);
        }
    }

    private fun animateRemoveView(event: Int) {
        when (event) {
            MotionEvent.ACTION_DOWN -> {
//                ObjectAnimator.ofFloat(removeFloatingView,"translationY",100f).apply {
//                    duration = 2000
//                    start()
//                }
//                removeFloatingView.animate().scaleX(1f).scaleY(1f).alpha(1f).translationY(100f).setDuration(300)
            }
            MotionEvent.ACTION_UP -> {

            }
        }
    }

    private fun updatePosition(view: View, params: WindowManager.LayoutParams) {
        try {
            Handler().postDelayed({
                if (params != null) {
                    if (params.x < (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() / 2)) {
                        if (params.x > 0) {
                            params.x -= 25
                            updatePosition(view, params)
                        } else {
                            params.x = 0
                        }
                    } else if (params.x < com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth()) {
                        params.x += 25
                        updatePosition(view, params)
                    } else {
                        params.x = com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth()
                    }

                    try {
                        manager.updateViewLayout(view, params)
                    } catch (ee: Exception) {
                    }
                }
            }, 10)
        } catch (e: Exception) {
        }
    }

    var isOtherFloatingOpened: Boolean = false
    private fun updatePositionStartStop(view: View, params: WindowManager.LayoutParams) {
        try {

            Handler().postDelayed({
                if (params != null) {
                    if (params.x < (com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() / 2)) {
//                        Log.d("screenRecordingParamsX",screenRecordingParams.x.toString())
                        if (params.x < 0 + com.scorpion.screenrecorder.utils.SRC_Utils.getScreenWidth() * 75 / 1080) {
                            params.x += 75 / 10
                        }
                        if (params.y > currentPositionY - com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight() * 175 / 1920) {
                            params.y -= 175 / 10
                        }
                        updatePositionStartStop(view, params)

                    }
//                    else if (params.x < Utils.getScreenWidth()) {
//                        params.x += 25
//                        updatePositionStartStop(view, params)
//                    } else {
//                        params.x = Utils.getScreenWidth()
//                    }

                    try {
                        manager.updateViewLayout(view, params)
                    } catch (ee: Exception) {
                    }
                }
            }, 10)
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()

//        stopCapture()
        if (mFloatingCameraView != null) mWindowManager!!.removeView(mCurrentView)
//        cameraView!!.stop()
        if (myReceiverIsRegistered) {
            myReceiverIsRegistered = false
            unregisterReceiver(StopRecoringOnScreenOff)
            unregisterReceiver(START_RECORDING_RECEIVER)
            unregisterReceiver(PLAY_PAUSE_RECORDING_RECEIVER)
            unregisterReceiver(SETTINGS_TOOLBOX_RECEIVER)
        }
        if (manager != null) {
            removeView(floatingView)
            removeView(brushFloatingView)
            removeView(screenRecordingFloatingView)
            removeView(screen_recording_home_intent_layout)
            removeView(screen_recording_settings_layout)
            removeView(screen_recording_start_stop_play_pause_layout)
        }
    }

    fun processImage(newPng: ByteArray,bitmap: Bitmap) {
        var path = ""
        thread {
            if (isScreenshotTaken) {
                isScreenshotTaken = false
                var screenshotFileName: String = System.currentTimeMillis().toString() + ".png";
                path =
                    getScreenshotDirPath(applicationContext).absolutePath + File.separator + screenshotFileName
                try {
                    val fos = FileOutputStream(path)
                    fos.write(newPng)
                    fos.flush()
                    fos.fd.sync()
                    fos.close()
                    excludeStatusAndNavigationFromScreenshot(path,bitmap)

                } catch (ee: Exception) {
                    Log.d("Exception123", ee.toString())
                }
            }
        }

//        showPreviewPopup(path)
        Handler().postDelayed({

        }, 2000)

        if (sharedPreferences.getBoolean("enableScreenshotSound", false) && startTone){
            startTone = false
            beeper.startTone(ToneGenerator.TONE_PROP_ACK)
        }
        if (!mIsRecording)
            stopCapture()
    }

    private fun excludeStatusAndNavigationFromScreenshot(path: String,bitmap1: Bitmap) {
        val bitmap = BitmapFactory.decodeFile(path)

        val currentBitmapHeight = bitmap.height
        val currentBitmapWidth = bitmap.width

        val statusHeight = (com.scorpion.screenrecorder.utils.SRC_Utils.getStatusBarHeight(applicationContext) * currentBitmapHeight) / com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight()
        val navigationHeight = (com.scorpion.screenrecorder.utils.SRC_Utils.getNavBarHeight(applicationContext) * currentBitmapHeight) / com.scorpion.screenrecorder.utils.SRC_Utils.getScreenHeight()

        Log.d("statusHeight", statusHeight.toString());
        Log.d("statusHeight11", navigationHeight.toString());

        val croppedBitmap: Bitmap =
            if (sharedPreferences.getBoolean("enableExcludeStatus", false) && sharedPreferences.getBoolean("enableExcludeNavigation", false)
            ) {
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    statusHeight,
                    currentBitmapWidth,
                    currentBitmapHeight - (statusHeight + navigationHeight)
                )
            } else if (sharedPreferences.getBoolean("enableExcludeStatus", false)) {
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    statusHeight,
                    currentBitmapWidth,
                    currentBitmapHeight - (statusHeight)
                )
            } else if (sharedPreferences.getBoolean("enableExcludeNavigation", false)) {
                Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    currentBitmapWidth,
                    currentBitmapHeight - (navigationHeight)
                )
            } else {
                Bitmap.createBitmap(bitmap, 0, 0, currentBitmapWidth, currentBitmapHeight)
            }

        //save bitmap

        val fileOutputStream = FileOutputStream(path)
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

        if (!mIsRecording && !isDrawingViewAdded)
            if (sharedPreferences.getBoolean("enableShowPreviewDialog", true))
                startActivity(
                    Intent(applicationContext, SRC_ScreenshotPreviewActivity::class.java)
                        .setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("position", 0)
                )
    }

    private fun stopCapture() {
        mediaProjection?.stop()
        virtualDisplay?.release()
        mediaProjection?.let { mediaProjection -> null }
    }

    override fun hearShake() {
        Log.d("hearShake", "hearShake")
        if (sharedPreferences.getBoolean("enableShake", false))
            takeScreenshot()
        if (sharedPreferences.getBoolean("enableStopOnShake", false)) {
            stopRecordingInGeneral()
//            stopRecording()
        }
    }

    //////////////////////////////
    //                          //
    //      SCREEN RECORDING    //
    //                          //
    //////////////////////////////

    private var mIsRecording = false
    private var mIsRecordingPause = false
    var mRecScheduled = false
    private var mMediaRecorder: MediaRecorder? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallback? = null
    var mOutputFile: File? = null

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            mMediaRecorder?.apply {
                stop()
                release()
            }
            mMediaRecorder = null
            mediaProjection = null
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay(
            TAG, width,
            height, resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mMediaRecorder?.surface, null, null
        )
    }

    private fun startRecording(data: Intent) {
        if (mIsRecording) {
            return
        }
        // We have started the recording, no more pending recordings.
        mRecScheduled = false
        // We are recording
        mIsRecording = true
        hideOtherFloatingView()
        screen_recording_start_stop_play_pause_layout_image_view?.setImageResource(R.drawable.src_f_stop_button)
        screen_recording_settings_image_view?.setImageResource(R.drawable.src_f_pause_button)
        contentView.setImageViewResource(R.id.homeIntent, R.drawable.src_n_pause_button)
        contentView.setImageViewResource(R.id.startRecording, R.drawable.src_n_stop_button)
        updateNoti()
        chronometer?.base = SystemClock.elapsedRealtime()
        chronometer?.start()
        screenRecordingImageView?.visibility = View.GONE
        chronometer?.visibility = View.VISIBLE
        try {
            mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, data)
            mediaProjection?.registerCallback(mMediaProjectionCallback, null)
        } catch (e: java.lang.Exception) {
        }
        // Init recorder
        initRecorder()
        // Create virtual display
        mVirtualDisplay = createVirtualDisplay()
        // Start recording
        mMediaRecorder?.start()

        if (sharedPreferences.getBoolean("enableTimeLimit", false))
            isTimeLimitEnabled()
        if (sharedPreferences.getBoolean("enableStopOnScreenOff", false))
            isScreenOffEnabled()

    }

    private fun isScreenOffEnabled() {

        val myKM = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (myKM.inKeyguardRestrictedInputMode()) {
            Log.d("status123", "locked")
        } else {
            Log.d("status123", "unlocked")
        }
    }

    var myReceiverIsRegistered: Boolean = false

    fun registerReceiver() {
        if (!myReceiverIsRegistered) {
            myReceiverIsRegistered = true
            registerReceiver(
                StopRecoringOnScreenOff,
                IntentFilter("com.mycompany.myapp.SOME_MESSAGE2")
            )
        }
    }

    val StopRecoringOnScreenOff: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                if (intent?.action.equals("com.mycompany.myapp.SOME_MESSAGE2")) {
                    if (intent?.getBooleanExtra("turn_off_recording", true)!!) {
                        if (sharedPreferences.getBoolean("enableStopOnScreenOff", false))
                            stopRecordingInGeneral()
//                        stopRecording()
                    }
                    if (intent.getStringExtra("whichFloating").equals("brush")) {
                        if (intent?.getBooleanExtra("turn_on_off_brush", false)!!)
                            brushFloatingView.visibility = View.VISIBLE
                        else
                            brushFloatingView.visibility = View.GONE
                    }
                    if (intent.getStringExtra("whichFloating").equals("screenShot")) {
                        if (intent?.getBooleanExtra("turn_on_off_enable_floating_screenShot", false)!!)
                            floatingView.visibility = View.VISIBLE
                        else
                            floatingView.visibility = View.GONE
                    }
                    if (intent.getStringExtra("whichFloating").equals("camera")) {
                        if (intent?.getBooleanExtra("turn_off_floating_camera", false)!!)
                            mFloatingCameraView?.visibility = View.VISIBLE
                        else
                            mFloatingCameraView?.visibility = View.GONE
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingTextViewColor")) {
                        var color = sharedPreferences.getInt(
                            "colorForText",
                            resources.getColor(R.color.tabSelectedIconColor)
                        )
                        floatingTextView.setTextColor(color)
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingTextViewBGColor")) {
                        var color = sharedPreferences.getInt(
                            "colorForBackgroundText",
                            resources.getColor(R.color.tabSelectedIconColor)
                        )
                        floatingTextView.setBackgroundColor(color)
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingTextViewSize")) {
                        floatingTextView.setTextSize(intent.getStringExtra("size")!!.toFloat())
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingTextView")) {
                        var color = sharedPreferences.getInt(
                            "colorForText",
                            resources.getColor(R.color.tabSelectedIconColor)
                        )
                        floatingTextView.setTextColor(color)
                        var colorBG = sharedPreferences.getInt(
                            "colorForBackgroundText",
                            resources.getColor(R.color.tabSelectedIconColor)
                        )
                        floatingTextView.setBackgroundColor(colorBG)
                        floatingTextView.setTextSize(
                            sharedPreferences.getString("textSize", "20")!!.toFloat()
                        )

                        if (intent?.getBooleanExtra("turn_on_off_floating_text_View", false)!!)
                            floatingTextViewLayout?.visibility = View.VISIBLE
                        else
                            floatingTextViewLayout?.visibility = View.GONE
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingLogo")) {
                        updateLogoFloating(intent)
                        if (intent?.getBooleanExtra("turn_on_off_floating_logo", false)!!)
                            floatingLogoLayout?.visibility = View.VISIBLE
                        else
                            floatingLogoLayout?.visibility = View.GONE
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingLogoChange")) {
                        Glide.with(applicationContext)
                            .load(sharedPreferences.getString("pathOfLogo", "")).into(floatingLogo)
                    }
                    if (intent.getStringExtra("whichFloating").equals("floatingLogoSize")) {
                        updateLogoFloating(intent)
                    }
                }

            }
            catch (e : java.lang.Exception){}
        }
    }

    fun updateLogoFloating(intent: Intent) {
        var widthPercent = (sharedPreferences.getInt("logoSize", 20) * 1080) / 100
        Log.d("size123", "$widthPercent")
        floatingLogo.layoutParams.width = widthPercent
        floatingLogo.layoutParams.height = widthPercent
        floatingLogoParams.x = widthPercent
        floatingLogoParams.y = widthPercent

        manager.updateViewLayout(floatingLogoLayout, floatingLogoParams)
    }

    private fun isTimeLimitEnabled() {
        object : CountDownTimer(
            sharedPreferences.getString("timeLimit", "600")!!.toLong() * 1000,
            1000
        ) {
            override fun onFinish() {
                stopRecordingInGeneral()
//                stopRecording()
            }

            override fun onTick(millisUntilFinished: Long) {
            }

        }.start()
    }

    private fun initRecorder() {
        mOutputFile =
            File("${SRC_FileUtils.getScreenRecordingDirPath(applicationContext).absolutePath}/${SRC_FileUtils.getFileName()}")

        if (mOutputFile == null) {
            Log.e(TAG, "Failed to get the file.")
            return
        }

        val isAudioRecEnabled = true

        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.reset()
        mMediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        if (isAudioRecEnabled) {
            mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        }
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder?.setOutputFile(mOutputFile?.path)
        // Set video size
        setVideoSize(mMediaRecorder!!)

        mMediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        val bitRate = sharedPreferences.getString("Video BitRate", "8200")!!.filter { it.isDigit() }
        mMediaRecorder?.setVideoEncodingBitRate(bitRate.toInt() * 1000)
        if (isAudioRecEnabled) {
            mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mMediaRecorder?.setAudioEncodingBitRate(
                sharedPreferences.getString(
                    "Audio BitRate",
                    "160"
                )!!.filter { it.isDigit() }.toInt() * 1000
            )
            mMediaRecorder?.setAudioSamplingRate(
                sharedPreferences.getString(
                    "Audio Sample Rate",
                    "24000"
                )!!.filter { it.isDigit() }.toInt()
            )
        }
        // Get user preference for frame rate
        val videoFrameRate =
            sharedPreferences.getString("Frame Rate", "30")!!.filter { it.isDigit() }.toInt()
        mMediaRecorder?.setVideoFrameRate(videoFrameRate)
        // Prepare MediaRecorder
        try {
            mMediaRecorder?.prepare()
        } catch (e: IOException) {
            Log.e(TAG, "prepare() failed")
        }
    }

    private fun setVideoSize(mMediaRecorder: MediaRecorder) {
        val res = sharedPreferences.getString("Resolution", "1080p")
        when {
            res.equals("1080p") -> {
                width = 1080
                height = 1920
            }
            res.equals("720p") -> {
                width = 720
                height = 1280
            }
            res.equals("480p") -> {
                width = 480
                height = 720
            }
            else -> {
                width = 360
                height = 480
            }
        }
        mMediaRecorder.setVideoSize(width, height)
    }

    fun stopRecording() {
        if (!mIsRecording) {
            return
        }
//        mIsRecording = false
        chronometer?.stop()
        chronometer?.visibility = View.GONE
        screenRecordingImageView?.visibility = View.VISIBLE
        screen_recording_start_stop_play_pause_layout_image_view?.setImageResource(R.drawable.src_f_record_button)
        screen_recording_settings_image_view?.setImageResource(R.drawable.src_f_setting_click)
        contentView.setImageViewResource(R.id.homeIntent, R.drawable.src_n_home_button)
        contentView.setImageViewResource(R.id.startRecording, R.drawable.src_n_record_button)
        updateNoti()
        // Stop the recording
        baseStopRecording()
        hideOtherFloatingView()
        mergeAllVideos()
    }

    fun stopRecordingForAPILevel24Below() {
        if (!mIsRecording) {
            return
        }
        mIsRecording = false
        hideOtherFloatingView()
        a_mrecorder.quit()
        chronometer?.stop()
        chronometer?.visibility = View.GONE
        screenRecordingImageView?.visibility = View.VISIBLE
        screen_recording_start_stop_play_pause_layout_image_view?.setImageResource(R.drawable.src_f_record_button)
        screen_recording_settings_image_view?.setImageResource(R.drawable.src_f_setting_click)
        contentView.setImageViewResource(R.id.homeIntent, R.drawable.src_n_home_button)
        contentView.setImageViewResource(R.id.startRecording, R.drawable.src_n_record_button)
        if (sharedPreferences.getBoolean("enableShowPreviewDialog", true))
            startActivity(
                Intent(applicationContext, SRC_VideoViewActivity::class.java)
                    .setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    .setFlags(FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("path", outputFileForAPILevel24Below)
            )
        updateNoti()
    }

    fun stopRecordingInGeneral() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopRecording()
        } else {
            stopRecordingForAPILevel24Below()
        }
    }

    fun hideOtherFloatingView() {
        isOtherFloatingVisible = false
        blackTrans.visibility = View.GONE

        Handler().postDelayed({
            screen_recording_home_intent_layout.visibility = View.GONE
            screen_recording_settings_layout.visibility = View.GONE
            screen_recording_start_stop_play_pause_layout.visibility = View.GONE
        }, 300)

        animSmallGone(screen_recording_home_intent_layout)
        animSmallGone(screen_recording_settings_layout)
        animSmallGone(screen_recording_start_stop_play_pause_layout)

        fadeWidget(screenRecordingFloatingView, screenRecordingParams)
    }

    private fun baseStopRecording() {
        // If we are not recording there's no need to get into all these actions
        if (!mIsRecording) {
            return
        }
        // Stopping the media recorder could lead to crash, let us be safe.
        mIsRecording = false
        // Remove all callbacks for the delayed recording
        mHandler.removeCallbacksAndMessages(null)
        mRecScheduled = false
        // Stop Media Recorder
        mMediaRecorder?.apply {
            stop()
            release()
        }
        mMediaRecorder = null
        // Stop screen sharing
        stopScreenSharing()
        // Destroy media projection session
        destroyMediaProjection()

        if (sharedPreferences.getBoolean("enableShowPreviewDialog", true))
            startActivity(
                Intent(applicationContext, SRC_VideoViewActivity::class.java)
                    .setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    .setFlags(FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("path", mOutputFile!!.absolutePath)
            )

        // Stop notification
//        stopForeground(true)
        // Stop shake service, we activate it after we start the recording for saving battery
//        mShakeDetector.stop()
        // Send broadcast for recording status
    }

    private fun stopScreenSharing() {
        // We don't have a virtual display anymore
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay?.release()
    }

    private fun destroyMediaProjection() {
        if (mediaProjection != null) {
            Log.d(TAG, "destroyMediaProjection()")
            mediaProjection?.unregisterCallback(mMediaProjectionCallback)
            mediaProjection?.stop()
            mediaProjection = null
        }
    }

    private fun mergeAllVideos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            val h264Track = H264TrackImpl(FileDataSourceImpl("video.h264"))
        }
    }


//    fun mergeMediaFiles(
//        isAudio: Boolean,
//        sourceFiles: ArrayList<String?>,
//        targetFile: String?
//    ): Boolean {
//        return try {
//
//            val h264Track = H264TrackImpl(FileDataSourceImpl("video.h264"))
//
//            val mediaKey = if (isAudio) "soun" else "vide"
//            var listMovies: MutableList<com.googlecode.mp4parser.authoring.Movie> = ArrayList()
//            for (filename in sourceFiles) {
//                listMovies.add(MovieCreator.build(filename))
//            }
//            var listTracks: MutableList<Track> = LinkedList()
//            for (movie in listMovies) {
//                for (track in movie.getTracks()) {
//                    if (track.getHandler().equals(mediaKey)) {
//                        listTracks.add(track)
//                    }
//                }
//            }
//            var outputMovie : com.googlecode.mp4parser.authoring.Movie ?= null
//            if (!listTracks.isEmpty()) {
//                outputMovie?.addTrack(AppendTrack(listTracks.toArray(object : Track())));
//            }
//            val container: Container = DefaultMp4Builder().build(outputMovie)
//            val fileChannel: FileChannel =
//                RandomAccessFile(String.format(targetFile!!), "rw").getChannel()
//            container.writeContainer(fileChannel)
//            fileChannel.close()
//            true
//        } catch (e: IOException) {
//            false
//        }
//    }


    var START_RECORDING_RECEIVER: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.sendBroadcast(Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"))

            if (mIsRecording) {
                stopRecordingInGeneral()
//                stopRecording()
                contentView.setImageViewResource(
                    R.id.startRecording,
                    R.drawable.src_n_record_button
                )
                updateNoti()
            } else {
                Log.d("rec123","adsdasdadfssa")
                showCountDownBeforeStartingRecording()
                contentView.setImageViewResource(
                    R.id.startRecording,
                    R.drawable.src_n_stop_button
                )
                updateNoti()
            }

        }
    }

    var PLAY_PAUSE_RECORDING_RECEIVER: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.sendBroadcast(Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"))
            if (mIsRecording)
                pauseResumeScreenRecording()
            else
                startActivity(
                    Intent(applicationContext, SRC_MainActivity::class.java)
                        .setFlags(FLAG_ACTIVITY_CLEAR_TOP)
                        .setFlags(FLAG_ACTIVITY_NEW_TASK)
                )
        }
    }

    var SETTINGS_TOOLBOX_RECEIVER: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            context.sendBroadcast(Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"))
            screenRecordingToolbox.visibility = View.VISIBLE
        }
    }


    //////////////////////////////
    //                          //
    //      FLOATING CAMERA     //
    //                          //
    //////////////////////////////


    private var mWindowManager: WindowManager? = null
    private var mFloatingCameraView: View? = null
    private var mCurrentView: View? = null
    private var resizeOverlay: ImageView? = null
    private var cameraView: CameraView? = null
    private var isCameraViewHidden = false
    private var values: Values? = null
    private var floatingCameraParams: WindowManager.LayoutParams? = null
    private var prefs: SharedPreferences? = null
    private var overlayResize = OverlayResize.MINWINDOW
    private val binder: IBinder = ServiceBinder()

    override fun onUnbind(intent: Intent): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }

    fun addFloatingCameraView() {
        val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mFloatingCameraView = li.inflate(R.layout.src_layout_floating_camera_view, null)
        cameraView = mFloatingCameraView!!.findViewById(R.id.cameraView)
        val hideCameraBtn = mFloatingCameraView!!.findViewById<ImageView>(R.id.hide_camera)
        val switchCameraBtn =
            mFloatingCameraView!!.findViewById<ImageView>(R.id.switch_camera)
        resizeOverlay = mFloatingCameraView!!.findViewById(R.id.overlayResize)
        values = Values()
        hideCameraBtn.setOnClickListener(this)
        switchCameraBtn.setOnClickListener(this)
        resizeOverlay?.setOnClickListener(this)
        mCurrentView = mFloatingCameraView
        val xPos = xPos
        val yPos = yPos
        val layoutType: Int
        layoutType =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        cameraView!!.facing = CameraView.FACING_FRONT

        //Add the view to the window.
        floatingCameraParams = WindowManager.LayoutParams(
            values!!.smallCameraX,
            values!!.smallCameraY,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        floatingCameraParams!!.gravity = Gravity.TOP or Gravity.START
        floatingCameraParams!!.x = xPos
        floatingCameraParams!!.y = yPos

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mCurrentView, floatingCameraParams)
        cameraView?.start()
        setupDragListener()
        mFloatingCameraView?.visibility = View.GONE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeCameraOrientation()
    }

    private fun changeCameraOrientation() {
        values!!.buildValues()
        val x =
            if (overlayResize == OverlayResize.MAXWINDOW) values!!.bigCameraX else values!!.smallCameraX
        val y =
            if (overlayResize == OverlayResize.MAXWINDOW) values!!.bigCameraY else values!!.smallCameraY
        if (!isCameraViewHidden) {
            floatingCameraParams!!.height = y
            floatingCameraParams!!.width = x
            mWindowManager!!.updateViewLayout(mCurrentView, floatingCameraParams)
        }
    }

    private fun setupDragListener() {
        mCurrentView!!.setOnTouchListener(object : View.OnTouchListener {
            var isMoving = false
            private val paramsF = floatingCameraParams
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isMoving = false
                        initialX = paramsF!!.x
                        initialY = paramsF.y
                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> if (!isMoving) {
                        showCameraView()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        val xDiff = (event.rawX - initialTouchX).toInt()
                        val yDiff = (event.rawY - initialTouchY).toInt()
                        paramsF!!.x = initialX + xDiff
                        paramsF.y = initialY + yDiff
                        /* Set an offset of 10 pixels to determine controls moving. Else, normal touches
                         * could react as moving the control window around */if (Math.abs(
                                xDiff
                            ) > 10 || Math.abs(yDiff) > 10
                        ) isMoving = true
                        mWindowManager!!.updateViewLayout(mCurrentView, paramsF)
                        persistCoordinates(initialX + xDiff, initialY + yDiff)
                        return true
                    }
                }
                return false
            }
        })
    }

    private val xPos: Int
        private get() {
            val pos = defaultPrefs!!.getString("camera_overlay_pos", "0X100")
            return pos!!.split("X".toRegex()).toTypedArray()[0].toInt()
        }

    private val yPos: Int
        private get() {
            val pos = defaultPrefs!!.getString("camera_overlay_pos", "0X100")
            return pos!!.split("X".toRegex()).toTypedArray()[1].toInt()
        }

    private fun persistCoordinates(x: Int, y: Int) {
        defaultPrefs!!.edit()
            .putString("camera_overlay_pos", x.toString() + "X" + y.toString())
            .apply()
    }

    private val defaultPrefs: SharedPreferences?
        private get() {
            if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(this)
            return prefs
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.switch_camera -> if (cameraView!!.facing == CameraView.FACING_BACK) {
                cameraView!!.facing = CameraView.FACING_FRONT
                cameraView!!.autoFocus = true
            } else {
                cameraView!!.facing = CameraView.FACING_BACK
                cameraView!!.autoFocus = true
            }
            R.id.hide_camera -> {
//                val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
//                intent.putExtra("turn_off_floating_camera", true)
//                sendBroadcast(intent)
                mFloatingCameraView?.visibility = View.GONE
                editor.putBoolean("enableCamera", false)
                editor.apply()
            }
            R.id.overlayResize -> updateCameraView()
        }
    }

    private fun showCameraView() {
//            mWindowManager?.removeViewImmediate(mCurrentView);
//            mCurrentView = mFloatingCameraView;
//            if (overlayResize == OverlayResize.MINWINDOW)
//                overlayResize = OverlayResize.MAXWINDOW;
//            else
//                overlayResize = OverlayResize.MINWINDOW;
//            mWindowManager?.addView(mCurrentView, floatingCameraParams);
//            isCameraViewHidden = false;
//            updateCameraView();
//            setupDragListener();
    }

    private fun updateCameraView() {
        if (overlayResize == OverlayResize.MINWINDOW) {
            floatingCameraParams!!.width = values!!.bigCameraX
            floatingCameraParams!!.height = values!!.bigCameraY
            overlayResize = OverlayResize.MAXWINDOW
            resizeOverlay!!.setImageResource(R.drawable.src_camera_minimize_button)
        } else {
            floatingCameraParams!!.width = values!!.smallCameraX
            floatingCameraParams!!.height = values!!.smallCameraY
            overlayResize = OverlayResize.MINWINDOW
            resizeOverlay!!.setImageResource(R.drawable.src_camera_full_screen_button)
        }
        mWindowManager!!.updateViewLayout(mCurrentView, floatingCameraParams)
    }

    private enum class OverlayResize {
        MAXWINDOW, MINWINDOW
    }

    private inner class Values {
        var smallCameraX = 0
        var smallCameraY = 0
        var bigCameraX = 0
        var bigCameraY = 0
        var cameraHideX: Int
        var cameraHideY: Int
        private fun dpToPx(dp: Int): Int {
            val displayMetrics =
                this@SRC_ScreenshotFloatingButtonService.resources.displayMetrics
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }

        fun buildValues() {
            val orientation = context.resources
                .configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                smallCameraX = dpToPx(160)
                smallCameraY = dpToPx(120)
                bigCameraX = dpToPx(200)
                bigCameraY = dpToPx(150)
            } else {
                smallCameraX = dpToPx(120)
                smallCameraY = dpToPx(160)
                bigCameraX = dpToPx(150)
                bigCameraY = dpToPx(200)
            }
        }

        init {
            buildValues()
            cameraHideX = dpToPx(60)
            cameraHideY = dpToPx(60)
        }
    }

    inner class ServiceBinder : Binder() {
        val service: SRC_ScreenshotFloatingButtonService
            get() = this@SRC_ScreenshotFloatingButtonService
    }

    private var context: SRC_ScreenshotFloatingButtonService

    init {
        context = this
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////
    //                                   //
    //      SCREEN RECORDING BELOW 24    //
    //                                   //
    ///////////////////////////////////////


    lateinit var a_mrecorder: SRC_ScreenRecorder
    lateinit var mvirtualdisplay: VirtualDisplay

    fun getCodecForAPILevel24Below() {
        SRC_Utils.findEncodersByTypeAsync(
            "video/avc",
            object : SRC_Utils.Callback {
                override fun onResult(infos: Array<out MediaCodecInfo>?) {
                    codecNameVideo = infos?.get(0)?.name.toString()
                    Log.d("codecNameVideo", infos?.get(0)?.name.toString())
                }
            })

        SRC_Utils.findEncodersByTypeAsync(
            "audio/mp4a-latm",
            object : SRC_Utils.Callback {
                override fun onResult(mediaCodecInfoArr: Array<MediaCodecInfo?>?) {
                    codecNameAudio = mediaCodecInfoArr?.get(0)?.name.toString()
                    Log.d("codecNameAudio", mediaCodecInfoArr?.get(0)?.name.toString())
                }
            })
    }

    var outputFileForAPILevel24Below: String = ""

    fun startRecordingForAPILevel24Below() {
        outputFileForAPILevel24Below =
            SRC_FileUtils.getScreenRecordingDirPath(applicationContext).absolutePath + "/" + SRC_FileUtils.getFileName()
        if (mIsRecording) {
            return
        }
        // We have started the recording, no more pending recordings.
        mRecScheduled = false
        // We are recording
        mIsRecording = true
        hideOtherFloatingView()
        screen_recording_start_stop_play_pause_layout_image_view?.setImageResource(R.drawable.src_f_stop_button)
        screen_recording_settings_image_view?.setImageResource(R.drawable.src_f_pause_button)
        contentView.setImageViewResource(R.id.homeIntent, R.drawable.src_n_pause_button)
        contentView.setImageViewResource(R.id.startRecording, R.drawable.src_n_stop_button)
        updateNoti()
        chronometer?.base = SystemClock.elapsedRealtime()
        chronometer?.start()
        screenRecordingImageView?.visibility = View.GONE
        chronometer?.visibility = View.VISIBLE

        if (sharedPreferences.getBoolean("enableTimeLimit", false))
            isTimeLimitEnabled()
        if (sharedPreferences.getBoolean("enableStopOnScreenOff", false))
            isScreenOffEnabled()

        setVideoSize()

        val bitRateVideo =
            sharedPreferences.getString("Video BitRate", "8200")!!.filter { it.isDigit() }
                .toInt() * 1000
        val bitRate = sharedPreferences.getString("Audio BitRate", "160")!!.filter { it.isDigit() }
            .toInt() * 1000
        var createVideoConfig =
            SRC_VideoEncodeConfig(
                width,
                height,
                25000000,
                30,
                1,
                codecNameVideo,
                "video/avc",
                null
            )
        var createAudioConfig =
            SRC_AudioEncodeConfig(
                codecNameAudio,
                "audio/mp4a-latm",
                320000,
                44100,
                1,
                1
            )
        mediaProjection =
            mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, resultDataRecording)
        mediaProjection?.registerCallback(mMediaProjectionCallback, null)

        a_mrecorder =
            SRC_ScreenRecorder(
                createVideoConfig,
                createAudioConfig,
                createVirtualDisplay2(),
                outputFileForAPILevel24Below
            )
        a_mrecorder.start()

//        Handler().postDelayed({
//            //pause
//            isPauseForAPI24LevelBelow = true
//            mLastPausedTimeUs = System.nanoTime() / 1000
//        },5000)
//        Handler().postDelayed({
//            //resume
//            if (mLastPausedTimeUs != 0L) {
//                offsetPTSUs += System.nanoTime() / 1000 - mLastPausedTimeUs
//                mLastPausedTimeUs = 0
//            }
//            isPauseForAPI24LevelBelow = false
//        },10000)
//        Handler().postDelayed({
//            a_mrecorder.quit()
//        },15000)

    }

    private fun setVideoSize() {
        val res = sharedPreferences.getString("Resolution", "1080p")
        when {
            res.equals("1080p") -> {
                width = 1080
                height = 1920
            }
            res.equals("720p") -> {
                width = 720
                height = 1280
            }
            res.equals("480p") -> {
                width = 480
                height = 720
            }
            else -> {
                width = 360
                height = 480
            }
        }
    }

    private fun createVirtualDisplay2(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay(
            TAG, width,
            height, 1,
            1,
            null, null, null
        )
    }

}