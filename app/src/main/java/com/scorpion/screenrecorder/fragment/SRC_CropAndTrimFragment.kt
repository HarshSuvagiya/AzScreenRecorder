package com.scorpion.screenrecorder.fragment

import android.app.ProgressDialog
import android.graphics.Rect
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
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.ayoubfletcher.consentsdk.ConsentSDK
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.InterstitialAd
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.appopen.SRC_AppOpenManager
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenRecordingDirPath
import com.scorpion.screenrecorder.utils.SRC_Utils
import com.scorpion.screenrecorder.window.SRC_VideoPlayer
import com.scorpion.screenrecorder.window.SRC_VideoSliceSeekBarH
import kotlinx.android.synthetic.main.src_activity_video_view.*
import kotlinx.android.synthetic.main.src_fragment_crop_and_trim.*
import kotlinx.android.synthetic.main.src_fragment_crop_and_trim.currentDuration
import kotlinx.android.synthetic.main.src_fragment_crop_and_trim.seekBar
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class SRC_CropAndTrimFragment : Fragment(), SRC_VideoPlayer.OnProgressUpdateListener,
    SRC_VideoSliceSeekBarH.SeekBarChangeListener {

    private var mVideoPlayer: SRC_VideoPlayer? = null
    private var formatBuilder: StringBuilder? = null
    private var formatter: Formatter? = null
    lateinit var path: String
    private var isVideoPlaying = false
    lateinit var progressDialog: ProgressDialog
    var handler = Handler()

    var seekRunnable: Runnable = object : Runnable {
        override fun run() {
            if (cropVideoView != null && videoView.isPlaying) {
                var currentPos = videoView.currentPosition
                seekBar.progress = currentPos
                currentDuration.text = formatTimeUnit(currentPos.toLong())
                handler.postDelayed(this, 100)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.src_fragment_crop_and_trim, container, false)

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Please wait!!!")
        progressDialog.setCancelable(false)

        loadInterstitial()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        path = arguments!!.getString("path").toString()

        Log.d("path1233", path)


        formatBuilder = java.lang.StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())

        cropVideoView.setFixedAspectRatio(false)
        initPlayer(path)
        export.setOnClickListener {
            SRC_AppOpenManager.needToShow = true
            MyAsync().execute()
        }

        SRC_Helper.setSize(ivPlay, 100, 100, true)
        SRC_Helper.setSize(export, 480, 1030, true)

        ivPlay.setOnClickListener(View.OnClickListener { playPause() })
    }

    private fun initPlayer(uri: String) {
        if (!File(uri).exists()) {
            Toast.makeText(activity, "File doesn't exists", Toast.LENGTH_SHORT).show()
            activity!!.finish()
            return
        }
        mVideoPlayer =
            SRC_VideoPlayer(
                activity
            )
        cropVideoView.setPlayer(mVideoPlayer!!.getPlayer())
        mVideoPlayer!!.initMediaSource(activity, uri)
        mVideoPlayer!!.setUpdateListener(this)
        handler.postDelayed(seekRunnable, 100)

        fetchVideoInfo(uri)
    }

    private fun playPause() {
        isVideoPlaying = !mVideoPlayer!!.isPlaying
        if (mVideoPlayer!!.isPlaying) {
            mVideoPlayer!!.play(!mVideoPlayer!!.isPlaying)
            tmbProgress.setSliceBlocked(false)
            tmbProgress.removeVideoStatusThumb()
            ivPlay.setImageResource(R.drawable.src_play_button)
            handler.removeCallbacks(seekRunnable)
            return
        }
        mVideoPlayer!!.seekTo(tmbProgress.getLeftProgress())
        mVideoPlayer!!.play(!mVideoPlayer!!.isPlaying)
        tmbProgress.videoPlayingProgress(tmbProgress.getLeftProgress())
        ivPlay.setImageResource(R.drawable.src_pause_button)
        handler.postDelayed(seekRunnable, 100)
    }

    private fun fetchVideoInfo(uri: String) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(File(uri).absolutePath)
        val videoWidth =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val videoHeight =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        val rotationDegrees =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
        cropVideoView.initBounds(videoWidth, videoHeight, rotationDegrees)
    }

    private fun handleCropStart() {
        val cropRect: Rect = cropVideoView.getCropRect()
        val startCrop: Long = tmbProgress.getLeftProgress()
        val durationCrop: Long = tmbProgress.getRightProgress() - tmbProgress.getLeftProgress()
        var start = Util.getStringForTime(formatBuilder, formatter, startCrop)
        var duration = Util.getStringForTime(formatBuilder, formatter, durationCrop)
        start += "." + startCrop % 1000
        duration += "." + durationCrop % 1000

        val crop = String.format(
            "crop=%d:%d:%d:%d:exact=0",
            cropRect.right,
            cropRect.bottom,
            cropRect.left,
            cropRect.top
        )

        Log.d("crop123", crop)
        val cmd = arrayOf(
            "-y",
            "-ss",
            start,
            "-i",
            path,
            "-t",
            duration,
            "-vf",
            crop,
            "${getScreenRecordingDirPath(requireContext()).absolutePath}${File.separator}${System.currentTimeMillis()}.mp4"
        )

        val rc = FFmpeg.execute(cmd)

        if (rc == RETURN_CODE_SUCCESS) {
            progressDialog.dismiss()
            Log.d("Done123", "done123")
        }
    }

    override fun onStart() {
        super.onStart()
        if (isVideoPlaying) {
            mVideoPlayer!!.play(true)
        }
    }

    override fun onStop() {
        super.onStop()
        mVideoPlayer!!.play(false)
    }

    override fun onProgressUpdate(currentPosition: Long, duration: Long, bufferedPosition: Long) {
        tmbProgress.videoPlayingProgress(currentPosition)
        if (!mVideoPlayer!!.isPlaying || currentPosition >= tmbProgress.getRightProgress()) {
            if (mVideoPlayer!!.isPlaying) {
                playPause()
            }
        }

        seekBar.max = duration.toInt()
        tmbProgress.setSliceBlocked(false)
        tmbProgress.removeVideoStatusThumb()

//        var currentPos = currentPosition
//        seekBar.progress = currentPosition.toInt()
//        currentDuration.text = formatTimeUnit(currentPos.toLong())
    }

    override fun onFirstTimeUpdate(duration: Long, currentPosition: Long) {
        tmbProgress.setSeekBarChangeListener(this)
        tmbProgress.setMaxValue(duration)
        tmbProgress.leftProgress = 0
        tmbProgress.rightProgress = duration
        tmbProgress.setProgressMinDiff(0)

    }

    override fun seekBarValueChanged(leftThumb: Long, rightThumb: Long) {
        if (tmbProgress.selectedThumb === 1) {
            mVideoPlayer!!.seekTo(leftThumb)
        }

        tvDuration.setText(Util.getStringForTime(formatBuilder, formatter, rightThumb))
        tvProgress.setText(Util.getStringForTime(formatBuilder, formatter, leftThumb))
    }

    inner class MyAsync : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            handleCropStart()
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
//            progressDialog.dismiss()
//            Toast.makeText(activity,"Done",Toast.LENGTH_LONG).show()
//            activity!!.finish()

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

    var interstitialAd: InterstitialAd? = null

    // Load Interstitial
    private fun loadInterstitial() {
        interstitialAd = InterstitialAd(activity)
        interstitialAd?.adUnitId = SRC_Utils.INTER_ID
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd?.loadAd(ConsentSDK.getAdRequest(activity))
    }
}