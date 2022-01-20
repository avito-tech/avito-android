package com.avito.upload_to_googleplay

import java.io.File

public data class GooglePlayDeploy(
    val binaryType: BinaryType,
    val track: String,
    val applicationId: String,
    val binary: File,
    val mapping: File
) {
    public enum class BinaryType { APK, BUNDLE }
}
