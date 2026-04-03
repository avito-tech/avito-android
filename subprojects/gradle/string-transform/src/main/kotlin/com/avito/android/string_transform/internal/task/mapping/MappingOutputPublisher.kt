package com.avito.android.string_transform.internal.task.mapping

import com.avito.android.Result
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

internal class MappingOutputPublisher {

    fun publish(sourceMapping: File, outputMapping: File): Result<Unit> = Result.tryCatch {
        outputMapping.parentFile.mkdirs()
        Files.copy(
            sourceMapping.toPath(),
            outputMapping.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
        )
    }
}
