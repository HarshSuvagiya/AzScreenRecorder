package com.scorpion.screenrecorder.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.scorpion.screenrecorder.R

open class SRC_BaseActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.src_activity_base)

        sharedPreferences = getSharedPreferences("myPref", 0)
        editor = sharedPreferences.edit()

    }
}