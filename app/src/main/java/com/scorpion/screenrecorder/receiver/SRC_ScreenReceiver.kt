package com.scorpion.screenrecorder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SRC_ScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            val intent = Intent("com.mycompany.myapp.SOME_MESSAGE2")
            intent.putExtra("turn_off_recording", true)
            context?.sendBroadcast(intent)
        } else if (intent?.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d("status123","on123")
        }
    }
}