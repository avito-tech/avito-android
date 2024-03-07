package com.avito.test.gradle.files

import com.avito.test.gradle.file
import java.io.File

internal fun File.androidManifest() = file(
    name = "AndroidManifest.xml",
    content = """
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
        </manifest>
    """.trimIndent()
)
