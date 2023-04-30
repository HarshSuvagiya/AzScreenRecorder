package com.scorpion.screenrecorder.fragment

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
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
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.scorpion.screenrecorder.R
import kotlinx.android.synthetic.main.src_fragment_add_audio.*
import kotlinx.android.synthetic.main.src_fragment_add_audio.bottomLayout
import kotlinx.android.synthetic.main.src_fragment_rotate_video.currentDuration
import kotlinx.android.synthetic.main.src_fragment_rotate_video.playPause
import kotlinx.android.synthetic.main.src_fragment_rotate_video.seekBar
import kotlinx.android.synthetic.main.src_fragment_rotate_video.videoView
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenRecordingDirPath
import com.scorpion.screenrecorder.utils.SRC_RealPathUtils
import com.scorpion.screenrecorder.utils.SRC_Utils
import java.io.File
import java.util.concurrent.TimeUnit


class SRC_AddAudioFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.src_fragment_add_audio, container, false)

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Please wait!!!")
        progressDialog.setCancelable(false)

        loadInterstitial()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        path = arguments!!.getString("path").toString()

        SRC_Helper.setSize(bottomLayout, 1080, 145, true)
        SRC_Helper.setSize(playPause, 90, 90, true)
        SRC_Helper.setSize(addAudio, 480, 130, true)



        addAudio.setOnClickListener {
            val intent_upload = Intent()
            intent_upload.setType("audio/*")
            intent_upload.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(intent_upload, 1)
        }

        videoView.setVideoPath(path)
        videoView.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer?) {
                duration = mp!!.duration
                seekBar.max = duration
                videoView.start()
                handler.postDelayed(seekRunnable, 100)
            }
        })
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
            addAudioToVideo()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
//            progressDialog.dismiss()
//            Toast.makeText(activity,"Done",Toast.LENGTH_LONG).show()
//            activity!!.finish()
            videoView.pause()
            if (interstitialAd!!.isLoaded) {
                interstitialAd!!.setAdListener(object : AdListener() {
                    override fun onAdClosed() {
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show()
                        activity!!.finish()
                    }
                })
                interstitialAd!!.show()
            } else {
                progressDialog.dismiss()
                Toast.makeText(activity, "Done", Toast.LENGTH_LONG).show()
                activity!!.finish()
            }
        }
    }

    private fun addAudioToVideo() {


        var cmd: Array<String> = arrayOf(
            "-y",
            "-i",
            path,
            "-i",
            audioPath,
            "-c:v",
            "copy",
            "-c:a",
            "aac",
            "-map",
            "0:v:0",
            "-map",
            "1:a:0",
            "${getScreenRecordingDirPath(requireContext()).absolutePath}${File.separator}${System.currentTimeMillis()}.mp4"
        )

        val rc = FFmpeg.execute(cmd)

        if (rc == Config.RETURN_CODE_SUCCESS) {
            Log.d("Done123", "done123")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            audioPath = SRC_RealPathUtils.getRealPath(activity, intent!!.data)
            MyAsync().execute()
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