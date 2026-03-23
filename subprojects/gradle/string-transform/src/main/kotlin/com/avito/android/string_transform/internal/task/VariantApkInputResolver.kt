package com.avito.android.string_transform.internal.task

import com.avito.android.Result
import java.io.File

internal class VariantApkInputResolver {

    fun observe(inputDirectory: File): Result<Unit> = Result.tryCatch {
        check(inputDirectory.exists()) {
            "VariantApkOutputs directory does not exist: ${inputDirectory.path}"
        }
        check(inputDirectory.isDirectory) {
            "VariantApkOutputs path is not a directory: ${inputDirectory.path}"
        }
    }

    fun resolveSingle(inputDirectory: File): Result<File> = Result.tryCatch {
        val apkFiles = inputDirectory.listFiles().orEmpty()
            .filter { it.isFile && it.extension == "apk" }
            .sortedBy { it.path }

        // The current implementation supports only a single publishable APK and does not handle split APK sets yet.
        require(apkFiles.size == 1) {
            "Observed VariantApkOutputs do not contain exactly one publishable APK candidate: " +
                apkFiles.joinToString(prefix = "[", postfix = "]") { it.path }
        }

        apkFiles.single()
    }
}
