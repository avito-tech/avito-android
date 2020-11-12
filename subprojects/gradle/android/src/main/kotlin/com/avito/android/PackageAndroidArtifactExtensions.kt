package com.avito.android

import com.android.build.gradle.tasks.PackageAndroidArtifact
import java.io.File

fun PackageAndroidArtifact.getApkFile(): File {
    val outputDir = outputDirectory.get().asFile

    return File(outputDir, TODO())
}
