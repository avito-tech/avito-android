package com.avito.android.test.screenshot_test.internal

import android.annotation.SuppressLint
import java.io.File

internal class ScreenshotDirectories(private val packageName: String) {

    operator fun get(type: String): File {
        return getSdcardDir(type)
    }

    private fun getSdcardDir(type: String): File {
        val externalStorage = System.getenv("EXTERNAL_STORAGE")
            ?: throw IllegalStateException(
                "No \$EXTERNAL_STORAGE has been set on the device"
            )
        val parent = "$externalStorage/$DEFAULT_SDCARD_DIRECTORY/$$packageName.test/"
        val child = "$parent/$type"
        File(parent).mkdirs()
        val dir = File(child)
        dir.mkdir()
        if (!dir.exists()) {
            throw RuntimeException("Failed to create the directory ${dir.absolutePath} for screenshots.")
        }
        setWorldWriteable(dir)
        return dir
    }

    @SuppressLint("SetWorldWritable")
    private fun setWorldWriteable(dir: File) {
        dir.setWritable( /* writeable = */true,  /* ownerOnly = */false)
    }
}

private const val DEFAULT_SDCARD_DIRECTORY = "screenshots"