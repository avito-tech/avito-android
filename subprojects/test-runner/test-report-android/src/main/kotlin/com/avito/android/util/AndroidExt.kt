package com.avito.android.util

import android.app.Activity
import android.graphics.Bitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.CountDownLatch

inline fun Activity.runOnMainThreadSync(crossinline block: () -> Unit) {
    val latch = CountDownLatch(1)
    runOnUiThread {
        try {
            block()
        } finally {
            latch.countDown()
        }
    }
    latch.await()
}

fun Bitmap.toPng(): InputStream {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, /* quality, ignored for PNG */ 0, outputStream)
    val png = outputStream.toByteArray()
    return ByteArrayInputStream(png)
}
