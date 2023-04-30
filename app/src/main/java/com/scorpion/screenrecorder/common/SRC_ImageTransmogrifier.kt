/***
 * Copyright (c) 2015 CommonsWare, LLC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 * by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * Covered in detail in the book _The Busy Coder's Guide to Android Development_
 * https://commonsware.com/Android
 */
package com.scorpion.screenrecorder.common

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Point
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.view.Display
import android.view.Surface
import com.scorpion.screenrecorder.service.SRC_ScreenshotFloatingButtonService
import java.io.ByteArrayOutputStream

@SuppressLint("WrongConstant")
class SRC_ImageTransmogrifier internal constructor(svc: SRC_ScreenshotFloatingButtonService) :
    OnImageAvailableListener {
    private val width: Int
    private val height: Int
    private val imageReader: ImageReader
    private val svc: SRC_ScreenshotFloatingButtonService
    private var latestBitmap: Bitmap? = null
    override fun onImageAvailable(reader: ImageReader) {
        val image = imageReader.acquireLatestImage()
        if (image != null) {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            val bitmapWidth = width + rowPadding / pixelStride
            if (latestBitmap == null || latestBitmap!!.width != bitmapWidth || latestBitmap!!.height != height
            ) {
                if (latestBitmap != null) {
                    latestBitmap!!.recycle()
                }
                latestBitmap = Bitmap.createBitmap(
                    bitmapWidth,
                    height, Bitmap.Config.ARGB_8888
                )
            }
            latestBitmap!!.copyPixelsFromBuffer(buffer)
            image.close()
            val baos = ByteArrayOutputStream()
            val cropped = Bitmap.createBitmap(
                latestBitmap!!, 0, 0,
                width, height
            )
            cropped.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val newPng = baos.toByteArray()
            svc.processImage(newPng,latestBitmap!!)
        }
    }

    fun getSurface(): Surface {
        return imageReader.surface
    }


    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }

    fun close() {
        imageReader.close()
    }

    init {
        this.svc = svc
        val display: Display = svc.getWindowManager().getDefaultDisplay()
        val size = Point()
        display.getRealSize(size)
        var width = size.x
        var height = size.y
        while (width * height > 2 shl 19) {
            width = width shr 1
            height = height shr 1
        }
        this.width = width
        this.height = height
        imageReader = ImageReader.newInstance(
            width, height,
            PixelFormat.RGBA_8888, 2
        )
        imageReader.setOnImageAvailableListener(this, svc.getHandler())
    }
}