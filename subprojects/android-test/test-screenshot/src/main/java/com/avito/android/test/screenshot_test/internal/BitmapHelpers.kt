package com.avito.android.test.screenshot_test.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("SdCardPath")
internal fun getBitmapFromDevice(context: Context, filePath: String): Bitmap {
    return FileInputStream(getFileFromDevice(context, filePath)).use {
        BitmapFactory.decodeStream(it)
    }
}

internal fun getBitmapFromAsset(context: Context, filePath: String): Bitmap {
    return context.assets.open("screenshots/$filePath.png").use {
        BitmapFactory.decodeStream(it)
    }
}

@SuppressLint("SdCardPath")
internal fun getFileFromDevice(context: Context, filePath: String): File {
    return File("/sdcard/screenshots/${context.packageName}.test/$filePath.png")
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
    getBitmapFromAsset(context, filePath).compress(
        Bitmap.CompressFormat.PNG,
        100,
        fileOutputStream
    )
    return File(resultFilePath)
}

private const val tag = "bitmap_helper"
