package com.avito.android

import com.android.build.gradle.tasks.PackageAndroidArtifact
import java.io.File

fun PackageAndroidArtifact.getApkFile(): File {
    require(apkNames.size == 1) {
        "Cannot get apk from $name android artifact task because apkNames.size != 1 ($apkNames). Split apk is not supported."
    }

    return File(
        outputDirectory.get().asFile,
        apkNames.first()
    )
}
