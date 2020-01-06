@file:JvmName("FileUtils")

package com.avito.http

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

fun File.convertToImageMultipart(name: String = "image", filename: String = "image"): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name,
        filename,
        RequestBody.create(MEDIA_TYPE_IMAGE, this)
    )
}

private val MEDIA_TYPE_IMAGE = MediaType.parse("image/*")
