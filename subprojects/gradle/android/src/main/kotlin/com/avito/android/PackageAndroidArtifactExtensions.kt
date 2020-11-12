package com.avito.android

import com.android.build.gradle.tasks.PackageAndroidArtifact
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import java.io.File

// TODO: Use Artifacts API
fun PackageAndroidArtifact.apkDirectory(): DirectoryProperty = outputDirectory

fun Directory.getApk(): File? {
    val apks = asFile.listFiles()
        .filter {
            it.extension == "apk"
        }

    require(apks.size < 2) {
        "Multiple APK are not supported. Dir: ${asFile}"
    }
    return apks.firstOrNull()
}

fun Directory.getApkOrThrow(): File {
    return requireNotNull(getApk()) {
        "APK not found in ${asFile}. Files in dir: ${asFile.listFiles().joinToString()}"
    }
}
