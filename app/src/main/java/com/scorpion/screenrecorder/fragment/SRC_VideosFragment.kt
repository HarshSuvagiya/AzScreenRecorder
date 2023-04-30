package com.scorpion.screenrecorder.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.adapter.SRC_ScreenRecordingAdapter
import com.scorpion.screenrecorder.service.SRC_ScreenshotFloatingButtonService
import kotlinx.android.synthetic.main.src_fragment_videos.*
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenRecordingDirPath
import java.io.File
import java.util.*

class SRC_VideosFragment : SRC_BaseFragment() {

    var fileArrayList = ArrayList<File>()
    lateinit var screenShotAdapter: SRC_ScreenRecordingAdapter
    lateinit var mContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = container!!.context
        val view = inflater.inflate(R.layout.src_fragment_videos, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SRC_Helper.setSize(info_Logo,878,384,true)

        if (!isMyServiceRunning(SRC_ScreenshotFloatingButtonService::class.java))
            if (sharedPreferences.getBoolean("enableFloating", true))
                startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 24)
    }

    override fun onResume() {
        super.onResume()

        DataLoader().execute()
    }

    fun getScreenShots() {

        lateinit var listFiles: Array<File>
        fileArrayList.clear()
        val file = getScreenRecordingDirPath(requireContext())
        if (file.exists()) {
            listFiles = file.listFiles()
            if (listFiles != null) {
                for (files in listFiles) {
                    Log.d("ext123", files.extension)
                    if (files.extension.equals("mp4"))
                        fileArrayList.add(files)
                }
            }
        }
    }

    inner class DataLoader : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            getScreenShots()

            fileArrayList.sortWith(Comparator { o1, o2 ->
                val s11 = o1?.lastModified()
                val s22 = o2?.lastModified()
                s22!!.compareTo(s11!!)
            })
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)

            if(fileArrayList.isEmpty()){
                info_Logo.visibility=View.VISIBLE
            }else{
                info_Logo.visibility=View.GONE
            }


            screenShotAdapter = SRC_ScreenRecordingAdapter(fileArrayList)
            screenRecordingRecyclerView.layoutManager = GridLayoutManager(mContext,3)
            screenRecordingRecyclerView.adapter = screenShotAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 24) {
            if (resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.startForegroundService(
                        Intent(activity, SRC_ScreenshotFloatingButtonService::class.java)
                            .putExtra(
                                SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_CODE,
                                requestCode
                            )
                            .putExtra(SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_INTENT, data)
                    )
                }
                else {
                    activity?.startService(
                        Intent(activity, SRC_ScreenshotFloatingButtonService::class.java)
                            .putExtra(
                                SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_CODE,
                                requestCode
                            )
                            .putExtra(SRC_ScreenshotFloatingButtonService.EXTRA_RESULT_INTENT, data)
                    )
                }
            }
            else{
                Log.d("Result", "onActivityResult: result")
            }
        }
    }


}
