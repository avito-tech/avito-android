package com.avito.android.test.screenshot_test.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@SuppressLint("SdCardPath")
internal fun getBitmapFromDevice(file: File): Bitmap {
    return FileInputStream(file).use {
        BitmapFactory.decodeStream(it)
    }
}

internal fun Context.getBitmapFromAsset(filePath: String): Bitmap {
    return assets.open(filePath).use {
        BitmapFactory.decodeStream(it)
    }
}

internal fun saveBitmap(bitmap: Bitmap, file: File) {
    FileOutputStream(file).use {
        bitmap.compress(
            Bitmap.CompressFormat.PNG,
            100,
            it
        )
        Log.i("ViewSaver", "successfully save screenshot to ${file.absolutePath}")
    }
}
