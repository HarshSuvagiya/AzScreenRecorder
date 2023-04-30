package com.scorpion.screenrecorder.model

import java.io.Serializable

class SRC_ImageData(
    var title: String,
    var imageUrl: String,
    var bucketName: String,
    var width: Int,
    var height: Int
)  : Serializable