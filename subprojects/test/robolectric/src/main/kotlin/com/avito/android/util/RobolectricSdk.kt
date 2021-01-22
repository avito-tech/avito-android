package com.avito.android.util

import org.robolectric.pluginapi.Sdk
import java.io.File
import java.nio.file.Path

@SuppressWarnings("NewApi")
class RobolectricSdk(
    apiLevel: Int,
    private val androidVersion: String,
    private val androidRevision: String,
    private val robolectricRevision: String
) : Sdk(apiLevel) {

    private val version: String by lazy {
        buildString {
            append(androidVersion)
            if (androidRevision.isNotEmpty()) {
                append('_')
                append(androidRevision)
            }
            append('-')
            append("robolectric")
            append('-')
            append(robolectricRevision)
        }
    }

    private val path: Path by lazy {
        val gradleHome: String? = System.getenv("GRADLE_USER_HOME")
            ?: System.getProperty("user.home")?.let { "$it/.gradle" }

        val cacheDir = "$gradleHome/caches/modules-2/files-2.1/org.robolectric/android-all/$version"
        val fileName = "android-all-$version.jar"

        File(cacheDir).walk()
            .find { it.name == fileName }
            ?.toPath()
            ?: error("Can't find file cached jar for api $apiLevel in $cacheDir with name $fileName")
    }

    override fun getJarPath(): Path = path

    override fun getUnsupportedMessage(): String = ""

    /**
     * Copy of [org.robolectric.plugins.DefaultSdkProvider.DefaultSdk] implementation
     */
    override fun verifySupportedSdk(testClassName: String?) {
        if (isKnown && !isSupported) {
            throw UnsupportedOperationException(
                "Failed to create a Robolectric sandbox: $unsupportedMessage"
            )
        }
    }

    override fun getAndroidVersion(): String = androidVersion

    /**
     * Copy of [org.robolectric.plugins.DefaultSdkProvider.DefaultSdk] implementation
     */
    override fun getAndroidCodeName(): String = "REL"

    override fun isSupported(): Boolean = true
}
