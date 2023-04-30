package com.scorpion.screenrecorder.activity

import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.scorpion.screenrecorder.SRC_Helper
import com.scorpion.screenrecorder.R
import com.scorpion.screenrecorder.utils.SRC_Utils

class SRC_SplashActivity : AppCompatActivity() {

    var db = FirebaseDatabase.getInstance().reference
    private lateinit var counter : CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_splash)

        SRC_Helper.FS(this)
        initCounter()
        init()

    }

    private fun initCounter() {
        counter = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                startActivity(Intent(applicationContext, SRC_StartActivity::class.java))
                finish()
            }

        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun init() {
        if (!isNetworkAvailable()) {
            counter.start()
        } else {
            db.child("ScreenRecorder").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    SRC_Utils.BANNER_ID = snapshot.child("BannerID").value.toString()
                    SRC_Utils.INTER_ID = snapshot.child("InterID").value.toString()
                    SRC_Utils.NATIVE_ID = snapshot.child("NativeID").value.toString()
                    SRC_Utils.APP_OPEN_ID = snapshot.child("AppOpenID").value.toString()
                    counter.start()

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

}