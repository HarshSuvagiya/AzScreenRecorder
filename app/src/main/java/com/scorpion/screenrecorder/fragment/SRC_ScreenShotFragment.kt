package com.scorpion.screenrecorder.fragment

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.adapter.SRC_ScreenShotAdapter
import kotlinx.android.synthetic.main.src_fragment_screenshot.*
import kotlinx.android.synthetic.main.src_fragment_screenshot.info_Logo
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.activity.SRC_ImageFolderActivity
import com.scorpion.screenrecorder.utils.SRC_FileUtils.Companion.getScreenshotDirPath
import java.io.File
import java.util.*

class SRC_ScreenShotFragment : Fragment() {

    var fileArrayList = ArrayList<File>()
    lateinit var screenShotAdapter: SRC_ScreenShotAdapter
    lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContext = container!!.context
        val view = inflater.inflate(R.layout.src_fragment_screenshot, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SRC_Helper.setSize(info_Logo,878,384,true)

    }

    override fun onResume() {
        super.onResume()

        DataLoader().execute()
    }

    fun getScreenShots() {
        lateinit var listFiles: Array<File>
        fileArrayList.clear()
        val file = getScreenshotDirPath(requireContext())
        if (file.exists()) {
            listFiles = file.listFiles()
            if (listFiles != null) {
                for (files in listFiles) {
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

            Collections.sort(fileArrayList, object : Comparator<File> {
                override fun compare(o1: File?, o2: File?): Int {
                    val s11 = o1?.lastModified()
                    val s22 = o2?.lastModified()
                    return s22!!.compareTo(s11!!)
                }
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

            screenShotAdapter = SRC_ScreenShotAdapter(fileArrayList)
            screenShotRecyclerView.layoutManager = GridLayoutManager(mContext,3)
            screenShotRecyclerView.adapter = screenShotAdapter
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.src_screenshots_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.stitchImage -> {
                startActivity(Intent(activity,
                    SRC_ImageFolderActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

}
