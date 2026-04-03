package com.avito.android.string_transform.internal.task.aab

import com.avito.android.Result
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class AabOutputPublisher {

    fun publish(sourceAab: File, outputAab: File): Result<Unit> = Result.tryCatch {
        outputAab.parentFile.mkdirs()
        Files.copy(
            sourceAab.toPath(),
            outputAab.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
