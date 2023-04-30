package com.scorpion.screenrecorder.fragment

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.ParseException
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.adapter.SRC_VideoBackgroundAdapter
import kotlinx.android.synthetic.main.src_fragment_add_background.*
import kotlinx.android.synthetic.main.src_fragment_add_background.bottomLayout
import kotlinx.android.synthetic.main.src_fragment_text_sticker.currentDuration
import kotlinx.android.synthetic.main.src_fragment_text_sticker.export
import kotlinx.android.synthetic.main.src_fragment_text_sticker.playPause
import kotlinx.android.synthetic.main.src_fragment_text_sticker.seekBar
import kotlinx.android.synthetic.main.src_fragment_text_sticker.videoView
import com.scorpion.screenrecorder.appopen.SRC_AppOpenManager
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getRootDirPath
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenRecordingDirPath
import com.scorpion.screenrecorder.utils.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_Utils
import java.io.File
import java.io.InputStream
import java.util.concurrent.TimeUnit

class SRC_AddBackgroundFragment : Fragment() {

    var count: Int = 0
    lateinit var path: String
    lateinit var audioPath: String
    var handler = Handler()
    var isVideoPause: Boolean = false
    var duration: Int = 0
    lateinit var progressDialog: ProgressDialog


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
        val view = inflater.inflate(R.layout.src_fragment_add_background, container, false)
        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Please wait!!!")
        progressDialog.setCancelable(false)

        loadInterstitial()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        path = arguments!!.getString("path").toString()

        com.scorpion.screenrecorder.SRC_Helper.setSize(bottomLayout,1080,145,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(playPause,90,90,true)
        com.scorpion.screenrecorder.SRC_Helper.setSize(export,480,130,true)

        com.scorpion.screenrecorder.SRC_Helper.setMargin(export,0,0,50,0)

        getImageFromAssets()

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

        export.setOnClickListener {
            SRC_AppOpenManager.needToShow = true
            MyAsync().execute()
        }
    }

    lateinit var finalBitmap: Bitmap

    private fun getImageFromAssets() {
        val images: Array<String> = activity!!.assets.list("videobg")!!
        var inputstream: InputStream = context!!.assets.open("videobg/${images[0]}")
        finalBitmap = BitmapFactory.decodeStream(inputstream)

        videoBGRecycler.apply {
            adapter = SRC_VideoBackgroundAdapter(images, object : SRC_VideoBackgroundAdapter.GetImageClick {
                override fun imageClick(pos: Int) {
                    var inputstream: InputStream = context.assets.open("videobg/${images[pos]}")
                    finalBitmap = BitmapFactory.decodeStream(inputstream)
//                    Glide.with(context).load(bitmap).into(imageBg)
                    imageBg.setImageBitmap(BitmapFactory.decodeStream(context.assets.open("videobg/${images[pos]}")))
//                    saveBitmap(bitmap)
                }
            })
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    fun saveBitmap(bitmap: Bitmap) {
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
            saveBitmap(finalBitmap)
        }

        override fun doInBackground(vararg params: Void?): Void? {
            addBackgroundToVideo()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
//            progressDialog.dismiss()
//            Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show()
//            activity!!.finish()

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

    lateinit var outputPath: String
    var videoWidth: Float = 0f
    var videoHeight: Float = 0f

    private fun addBackgroundToVideo() {

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toFloat()
        videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toFloat()
        Log.d("videoWidth", videoWidth.toString())

        var cmd2: Array<String> = arrayOf(
            "-y",
            "-i",
            "${getRootDirPath(requireContext()).absolutePath}${File.separator}image.png",
            "-i",
            path,
            "-filter_complex",
            "[0:v]scale=1920:1080[v0];[1:v]scale=607:1080[v1];[v0][v1]overlay=x=${((1920 - videoWidth) / 2) + videoWidth / 4}:y=0",
            "-preset",
            "ultrafast",
            "-c:a",
            "copy",
            "${getScreenRecordingDirPath(requireContext()).absolutePath}${File.separator}${System.currentTimeMillis()}.mp4"
        )
//-filter_complex "[1]fps=25[v];[0][v]overlay=(W-w)/2:(H-h)/2:shortest=1,format=yuv420p" \

        val rc = FFmpeg.execute(cmd2)

        if (rc == Config.RETURN_CODE_SUCCESS) {
            Log.d("Done123", "done123")
        }
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