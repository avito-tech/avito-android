package com.avito.android.string_transform.internal.task

import com.avito.android.Result
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class OutputPublisher {

    fun publish(sourceFile: File, outputFile: File): Result<Unit> = Result.tryCatch {
        outputFile.parentFile.mkdirs()
        Files.copy(
            sourceFile.toPath(),
            outputFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
