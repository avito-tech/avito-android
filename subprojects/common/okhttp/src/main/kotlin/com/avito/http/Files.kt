@file:JvmName("FileUtils")

package com.avito.http

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun File.convertToImageMultipart(name: String = "image", filename: String = "image"): MultipartBody.Part {
    return MultipartBody.Part.createFormData(
        name,
        filename,
        this.asRequestBody(MEDIA_TYPE_IMAGE)
    )
}

private val MEDIA_TYPE_IMAGE = "image/*".toMediaType()
