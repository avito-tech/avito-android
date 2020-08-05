package com.avito.android.test.screenshot_test.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@SuppressLint("SdCardPath")
internal fun getBitmapFromDevice(context: Context, filePath: String): Bitmap? {
    val file = File("/sdcard/screenshots/${context.packageName}.test/$filePath.png")
    var bitmap: Bitmap? = null
    try {
        bitmap = BitmapFactory.decodeStream(FileInputStream(file))
    } catch (exception: Exception) {
        Log.e(tag, "can`t get image from device", exception)
    }
    return bitmap
}

@SuppressLint("SdCardPath")
internal fun getFileFromDevice(context: Context, filePath: String): File {
    return File("/sdcard/screenshots/${context.packageName}.test/$filePath.png")
}

internal fun getBitmapFromAsset(context: Context, filePath: String): Bitmap? {
    val assetManager = context.assets
    val istr: InputStream
    var bitmap: Bitmap? = null
    try {
        istr = assetManager.open("screenshots/$filePath.png")
        bitmap = BitmapFactory.decodeStream(istr)
    } catch (exception: IOException) {
        Log.e(tag, "can`t get image from asset", exception)
    }
    return bitmap
}

@SuppressLint("SdCardPath", "SetWorldWritable")
internal fun getFileFromAsset(context: Context, filePath: String): File {
    val deviceDirectoryName = DeviceDirectoryName.create(context).name
    val path = "/sdcard/reference_screenshots/${context.packageName}.test/"
    File("$path/$deviceDirectoryName").apply {
        mkdirs()
        setWritable(true, false)
    }
    val resultFilePath = "$path$filePath.png"
    val fileOutputStream = FileOutputStream(resultFilePath)
    getBitmapFromAsset(context, filePath)!!.compress(
        Bitmap.CompressFormat.PNG,
        100,
        fileOutputStream
    )
    return File(resultFilePath)
}

private const val tag = "bitmap_helper"