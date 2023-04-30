package com.scorpion.screenrecorder.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.media.MediaMetadataRetriever
import android.net.ParseException
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.divyanshu.colorseekbar.ColorSeekBar
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.scorpion.screenrecorder.R
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import kotlinx.android.synthetic.main.src_fragment_text_sticker.*
import kotlinx.android.synthetic.main.src_fragment_text_sticker.bottomLayout
import kotlinx.android.synthetic.main.src_fragment_text_sticker.currentDuration
import kotlinx.android.synthetic.main.src_fragment_text_sticker.playPause
import kotlinx.android.synthetic.main.src_fragment_text_sticker.seekBar
import kotlinx.android.synthetic.main.src_fragment_text_sticker.videoView
import com.scorpion.screenrecorder.appopen.SRC_AppOpenManager
import com.scorpion.screenrecorder.imageeditor.SRC_TextEditorDialogFragment
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getRootDirPath
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenRecordingDirPath
import com.scorpion.screenrecorder.utils.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_MultiTouchListener
import com.scorpion.screenrecorder.utils.SRC_Utils
import java.io.File
import java.util.concurrent.TimeUnit

class SRC_TextStickerFragment : Fragment(), OnPhotoEditorListener {

    var count: Int = 0
    lateinit var path: String
    lateinit var audioPath: String
    var handler = Handler()
    var isVideoPause: Boolean = false
    var duration: Int = 0
    lateinit var progressDialog: ProgressDialog
    var videoWidth: Float = 0f
    var videoHeight: Float = 0f
    lateinit var myview: View
//    var mPhotoEditor: PhotoEditor? = null
//    private var mPhotoEditorView: PhotoEditorView? = null

    var seekRunnable: Runnable = object : Runnable {
        override fun run() {
            if (videoView != null && videoView.isPlaying) {
                var currentPos = videoView.currentPosition
                seekBar.progress = currentPos
                currentDuration.text = formatTimeUnit(currentPos.toLong())
                handler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.src_fragment_text_sticker, container, false)
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Please wait!!!")
        progressDialog.setCancelable(false)

        loadInterstitial()
        return view
    }

    private fun handleIntentImage(source: ImageView) {
        source.setImageResource(R.drawable.src_trans)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.scorpion.screenrecorder.SRC_Helper.setSize(bottomLayout,1080,145,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(playPause,90,90,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(addText,480,130,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(export,480,130,true)

        com.scorpion.screenrecorder.SRC_Helper.setMargin(addText,50,0,0,0)
        com.scorpion.screenrecorder.SRC_Helper.setMargin(export,0,0,50,0)

        path = arguments!!.getString("path").toString()
        export.setOnClickListener {
            SRC_AppOpenManager.needToShow = true
            for (i in 1..count) {
                addTextSticker.findViewWithTag<View>(i).findViewById<ImageView>(R.id.removeFloatingTextView).visibility = View.GONE
            }
            saveBitmap()
            MyAsync().execute()
        }

        videoView.setVideoPath(path)
        videoView.setOnPreparedListener { mp ->
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

        addText.setOnClickListener {
            addTextView()
        }
    }

    fun saveBitmap() {
        var bitmap = SRC_Helper.getBitmapFromView(addTextSticker)
        SRC_Helper.saveBitmap("${getRootDirPath(requireContext()).absolutePath}${File.separator}image.png", bitmap)
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

    inner class MyAsync : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            addTextToVideo()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            videoView.pause()
            if (interstitialAd!!.isLoaded) {
                interstitialAd!!.setAdListener(object : AdListener() {
                    override fun onAdClosed() {
                        progressDialog.dismiss()
                        Toast.makeText(activity,"Done",Toast.LENGTH_LONG).show()
                        activity!!.finish()
                    }
                })
                interstitialAd!!.show()
            } else {
                progressDialog.dismiss()
                Toast.makeText(activity,"Done",Toast.LENGTH_LONG).show()
                activity!!.finish()
            }
        }
    }


    lateinit var outputPath : String
    private fun addTextToVideo() {

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toFloat()
        videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toFloat()

        var cmd: Array<String> = arrayOf(
            "-y",
            "-i",
            path,
            "-i",
            "${getRootDirPath(requireContext()).absolutePath}${File.separator}image.png",
            "-filter_complex",
            "[0:v]scale=${videoWidth}:${videoHeight}[v0];[1:v]scale=${videoWidth}:${videoHeight}[v1];[v0][v1]overlay",
            "-preset",
            "ultrafast",
            "-c:a",
            "copy",
            "${getScreenRecordingDirPath(requireContext()).absolutePath}${File.separator}${System.currentTimeMillis()}.mp4"
        )

        val rc = FFmpeg.execute(cmd)

        if (rc == Config.RETURN_CODE_SUCCESS) {
            Log.d("Done123", "done123")
        }
    }

    fun addTextView() {

        myview = LayoutInflater.from(activity).inflate(R.layout.src_floating_textview, null, false)
        val textView: TextView = myview.findViewById(R.id.floatingTV)
        val remove: ImageView = myview.findViewById(R.id.removeFloatingTextView)

        val dialog = Dialog(activity!!)
        dialog.setContentView(R.layout.src_add_floating_text_dialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        val editBg: ConstraintLayout = dialog.findViewById(R.id.editBg)
        val popup_Bg: CardView = dialog.findViewById(R.id.popup_Bg)
        val line: ImageView = dialog.findViewById(R.id.line)
        val editText: EditText = dialog.findViewById(R.id.add_text_edit_text)
        val colorSeekBar: ColorSeekBar = dialog.findViewById(R.id.color_seek_bar)
        val cancel: TextView = dialog.findViewById(R.id.cancel)
        val ok: TextView = dialog.findViewById(R.id.ok)

        com.scorpion.screenrecorder.SRC_Helper.setSize(popup_Bg,870,692,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(line,768,4,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(editBg,654,134,true)

        com.scorpion.screenrecorder.SRC_Helper.setMargin(line,0,50,0,0)

        cancel.setOnClickListener {
            dialog.dismiss()
        }
        ok.setOnClickListener {
            if (editText.text.trim().length != 0) {
                textView.text = editText.text
                count++
                myview.tag = count
                myview.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT).apply {
                    addRule(
                        RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE
                    )
                }
                myview.setOnTouchListener(SRC_MultiTouchListener())
                remove.setOnClickListener {
                    myview.visibility = View.GONE
                }

                addTextSticker.addView(myview)
            } else
                Toast.makeText(activity, "Please enter some text", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        colorSeekBar.setOnColorChangeListener(object : ColorSeekBar.OnColorChangeListener {
            override fun onColorChangeListener(color: Int) {
                editText.setTextColor(color)
                textView.setTextColor(color)
            }
        })

    }

    fun addTextView2() {
        val textEditorDialogFragment = SRC_TextEditorDialogFragment.show(activity as AppCompatActivity)
        textEditorDialogFragment.setOnTextEditorListener { inputText, colorCode ->
            val styleBuilder = TextStyleBuilder()
            styleBuilder.withTextColor(colorCode)
//            mPhotoEditor!!.addText(inputText, styleBuilder)
        }
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        val textEditorDialogFragment = SRC_TextEditorDialogFragment.show(activity as AppCompatActivity, text!!, colorCode)
        textEditorDialogFragment.setOnTextEditorListener { inputText, colorCode ->
            val styleBuilder = TextStyleBuilder()
            styleBuilder.withTextColor(colorCode)
//            mPhotoEditor!!.editText(rootView!!, inputText, styleBuilder)
        }
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
    }

    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
    }
    var interstitialAd: InterstitialAd? = null

    // Load Interstitial
    private fun loadInterstitial() {
        interstitialAd = InterstitialAd(activity)
        interstitialAd?.adUnitId = SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd?.loadAd(ConsentSDK.getAdRequest(activity))
    }
}