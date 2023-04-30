/*
 * Copyright (c) 2016-2018. Vijai Chandra Prasad R.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */
package com.scorpion.screenrecorder.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.android.cameraview.CameraView
import com.scorpion.screenrecorder.R

/**
 * Todo: Add class description here
 *
 * @author Vijai Chandra Prasad .R
 */
class SRC_FloatingCameraViewService : Service(), View.OnClickListener {
    private var mWindowManager: WindowManager? = null
    private var mFloatingView: LinearLayout? = null
    private var mCurrentView: View? = null
    private var resizeOverlay: ImageButton? = null
    private var cameraView: CameraView? = null
    private var isCameraViewHidden = false
    private var values: Values? = null
    private var params: WindowManager.LayoutParams? = null
    private var prefs: SharedPreferences? = null
    private var overlayResize = OverlayResize.MINWINDOW
    private val binder: IBinder = ServiceBinder()
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val li =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mFloatingView = li.inflate(R.layout.src_layout_floating_camera_view, null) as LinearLayout
        cameraView = mFloatingView!!.findViewById(R.id.cameraView)
        val hideCameraBtn = mFloatingView!!.findViewById<ImageButton>(R.id.hide_camera)
        val switchCameraBtn =
            mFloatingView!!.findViewById<ImageButton>(R.id.switch_camera)
        resizeOverlay = mFloatingView!!.findViewById(R.id.overlayResize)
        values = Values()
        hideCameraBtn.setOnClickListener(this)
        switchCameraBtn.setOnClickListener(this)
        resizeOverlay?.setOnClickListener(this)
        mCurrentView = mFloatingView
        val xPos = xPos
        val yPos = yPos
        val layoutType: Int
        layoutType =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_PHONE else WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        cameraView!!.facing = CameraView.FACING_FRONT

        //Add the view to the window.
        params = WindowManager.LayoutParams(
            values!!.smallCameraX,
            values!!.smallCameraY,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        //Specify the view position
        params!!.gravity = Gravity.TOP or Gravity.START
        params!!.x = xPos
        params!!.y = yPos

        //Add the view to the window
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mCurrentView, params)
        cameraView?.start()
        setupDragListener()
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeCameraOrientation()
    }

    private fun changeCameraOrientation() {
        values!!.buildValues()
        val x =
            if (overlayResize == OverlayResize.MAXWINDOW) values!!.bigCameraX else values!!.smallCameraX
        val y =
            if (overlayResize == OverlayResize.MAXWINDOW) values!!.bigCameraY else values!!.smallCameraY
        if (!isCameraViewHidden) {
            params!!.height = y
            params!!.width = x
            mWindowManager!!.updateViewLayout(mCurrentView, params)
        }
    }

    private fun setupDragListener() {
        mCurrentView!!.setOnTouchListener(object : OnTouchListener {
            var isMoving = false
            private val paramsF = params
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        isMoving = false
                        initialX = paramsF!!.x
                        initialY = paramsF.y
                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> if (!isMoving) {
                        showCameraView()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        val xDiff = (event.rawX - initialTouchX).toInt()
                        val yDiff = (event.rawY - initialTouchY).toInt()
                        paramsF!!.x = initialX + xDiff
                        paramsF.y = initialY + yDiff
                        /* Set an offset of 10 pixels to determine controls moving. Else, normal touches
                         * could react as moving the control window around */if (Math.abs(
                                xDiff
                            ) > 10 || Math.abs(yDiff) > 10
                        ) isMoving = true
                        mWindowManager!!.updateViewLayout(mCurrentView, paramsF)
                        persistCoordinates(initialX + xDiff, initialY + yDiff)
                        return true
                    }
                }
                return false
            }
        })
    }

    private val xPos: Int
        private get() {
            val pos = defaultPrefs!!.getString("camera_overlay_pos", "0X100")
            return pos!!.split("X".toRegex()).toTypedArray()[0].toInt()
        }

    private val yPos: Int
        private get() {
            val pos = defaultPrefs!!.getString("camera_overlay_pos", "0X100")
            return pos!!.split("X".toRegex()).toTypedArray()[1].toInt()
        }

    private fun persistCoordinates(x: Int, y: Int) {
        defaultPrefs!!.edit()
            .putString("camera_overlay_pos", x.toString() + "X" + y.toString())
            .apply()
    }

    private val defaultPrefs: SharedPreferences?
        private get() {
            if (prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(this)
            return prefs
        }

    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mCurrentView)
            cameraView!!.stop()

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.switch_camera -> if (cameraView!!.facing == CameraView.FACING_BACK) {
                cameraView!!.facing = CameraView.FACING_FRONT
                cameraView!!.autoFocus = true
            } else {
                cameraView!!.facing = CameraView.FACING_BACK
                cameraView!!.autoFocus = true
            }
            R.id.hide_camera -> {
                val intent = Intent("com.mycompany.myapp.SOME_MESSAGE")
                intent.putExtra("turn_off_camera", true)
                sendBroadcast(intent)
            }
            R.id.overlayResize -> updateCameraView()
        }
    }

    private fun showCameraView() {
//            mWindowManager?.removeViewImmediate(mCurrentView);
//            mCurrentView = mFloatingView;
//            if (overlayResize == OverlayResize.MINWINDOW)
//                overlayResize = OverlayResize.MAXWINDOW;
//            else
//                overlayResize = OverlayResize.MINWINDOW;
//            mWindowManager?.addView(mCurrentView, params);
//            isCameraViewHidden = false;
//            updateCameraView();
//            setupDragListener();
    }

    private fun updateCameraView() {
        if (overlayResize == OverlayResize.MINWINDOW) {
            params!!.width = values!!.bigCameraX
            params!!.height = values!!.bigCameraY
            overlayResize = OverlayResize.MAXWINDOW
            resizeOverlay!!.setImageResource(R.drawable.ic_bigscreen_exit)
        } else {
            params!!.width = values!!.smallCameraX
            params!!.height = values!!.smallCameraY
            overlayResize = OverlayResize.MINWINDOW
            resizeOverlay!!.setImageResource(R.drawable.ic_bigscreen)
        }
        mWindowManager!!.updateViewLayout(mCurrentView, params)
    }

    private enum class OverlayResize {
        MAXWINDOW, MINWINDOW
    }

    private inner class Values {
        var smallCameraX = 0
        var smallCameraY = 0
        var bigCameraX = 0
        var bigCameraY = 0
        var cameraHideX: Int
        var cameraHideY: Int
        private fun dpToPx(dp: Int): Int {
            val displayMetrics =
                this@SRC_FloatingCameraViewService.resources.displayMetrics
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
        }

        fun buildValues() {
            val orientation = context.resources
                .configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                smallCameraX = dpToPx(160)
                smallCameraY = dpToPx(120)
                bigCameraX = dpToPx(200)
                bigCameraY = dpToPx(150)
            } else {
                smallCameraX = dpToPx(120)
                smallCameraY = dpToPx(160)
                bigCameraX = dpToPx(150)
                bigCameraY = dpToPx(200)
            }
        }

        init {
            buildValues()
            cameraHideX = dpToPx(60)
            cameraHideY = dpToPx(60)
        }
    }

    inner class ServiceBinder : Binder() {
        val service: SRC_FloatingCameraViewService
            get() = this@SRC_FloatingCameraViewService
    }

    private var context: SRC_FloatingCameraViewService

    init {
        context = this
    }
}