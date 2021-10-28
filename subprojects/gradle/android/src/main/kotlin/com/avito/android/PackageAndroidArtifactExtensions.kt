package com.avito.android

import org.gradle.api.file.Directory
import java.io.File

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
