package com.avito.test.gradle.files

import com.avito.test.gradle.file
import java.io.File

internal fun File.androidManifest(packageName: String) = file(
    "AndroidManifest.xml", """
    <manifest package="$packageName"
        xmlns:android="http://schemas.android.com/apk/res/android">
    </manifest>
""".trimIndent()
)
