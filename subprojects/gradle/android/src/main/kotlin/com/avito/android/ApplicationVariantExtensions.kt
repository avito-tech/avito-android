@file:Suppress("UnstableApiUsage")

package com.avito.android

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.provider.Provider
import java.io.File

fun ApplicationVariant.apkFileProvider(): Provider<File> = packageApplicationProvider.map { it.getApkFile() }

fun ApplicationVariant.bundleFileProvider(): Provider<File> = apkFileProvider().map { apkFile ->
    val bundleDir = apkFile.parentFile.absolutePath.replace("apk", "bundle")
    val bundleName = apkFile.name
        .replace(".apk", ".aab")
        .replace("-unsigned", "")
    File(bundleDir, bundleName)
}
