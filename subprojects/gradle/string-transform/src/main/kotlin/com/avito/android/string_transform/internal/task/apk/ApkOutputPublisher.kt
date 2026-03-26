package com.avito.android.string_transform.internal.task.apk

import com.avito.android.Result
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class ApkOutputPublisher {

    fun publish(sourceApk: File, outputApk: File): Result<Unit> = Result.tryCatch {
        outputApk.parentFile.mkdirs()
        Files.copy(
            sourceApk.toPath(),
            outputApk.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
