package com.avito.android

import com.android.build.gradle.tasks.PackageAndroidArtifact
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import java.io.File

// TODO: Use Artifacts API
public fun PackageAndroidArtifact.apkDirectory(): DirectoryProperty = outputDirectory

public fun Directory.getApk(): File? {
    val dir = asFile
    val apks = dir.listFiles().orEmpty()
        .filter {
            it.extension == "apk"
        }

    require(apks.size < 2) {
        "Multiple APK are not supported: ${dir.dumpFiles()}"
    }
    return apks.firstOrNull()
}

public fun Directory.getApkOrThrow(): File {
    return requireNotNull(getApk()) {
        "APK not found in $asFile. Files in dir: ${asFile.dumpFiles()}"
    }
}

private fun File.dumpFiles(): String {
    return listFiles().orEmpty()
        .joinToString(prefix = "[", postfix = "]") { it.path }
}
