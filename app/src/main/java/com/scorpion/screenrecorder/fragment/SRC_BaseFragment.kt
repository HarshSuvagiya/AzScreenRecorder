package com.scorpion.screenrecorder.fragment


import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.scorpion.screenrecorder.R

/**
 * A simple [Fragment] subclass.
 */
open class SRC_BaseFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = activity!!.getSharedPreferences("myPref", 0)
        editor = sharedPreferences.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.src_fragment_base, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaProjectionManager = activity?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


}
