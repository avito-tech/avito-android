package com.avito.android.string_transform.internal.task.mapping

import com.avito.android.Result
import com.avito.retrace.ProguardRetracer
import java.io.File

internal class MappingStructuralSanityValidator {

    fun validate(mappingFile: File): Result<Unit> = Result.tryCatch {
        check(mappingFile.exists()) {
            "Transformed mapping file does not exist: ${mappingFile.path}"
        }
        check(mappingFile.isFile) {
            "Transformed mapping path is not a file: ${mappingFile.path}"
        }

        try {
            validateLineShape(mappingFile)
            ProguardRetracer.create(listOf(mappingFile))
                .retrace(SYNTHETIC_STACKTRACE)
        } catch (e: Throwable) {
            throw IllegalStateException(
                "Transformed mapping failed structural sanity validation: ${mappingFile.path}",
                e,
            )
        }
        Unit
    }

    private fun validateLineShape(mappingFile: File) {
        val hasInvalidLine = mappingFile.useLines { lines ->
            lines.any { line ->
                val trimmed = line.trim()
                trimmed.isNotEmpty() &&
                    !trimmed.startsWith("#") &&
                    !CLASS_MAPPING_PATTERN.matches(line) &&
                    !MEMBER_MAPPING_PATTERN.matches(line)
            }
        }

        check(!hasInvalidLine) {
            "Transformed mapping contains lines outside the supported proguard mapping shape"
        }
    }

    private companion object {
        private const val SYNTHETIC_STACKTRACE = "at a.a.a(:1)"
        private val CLASS_MAPPING_PATTERN = Regex("""^[^\s#].* -> .+:$""")
        private val MEMBER_MAPPING_PATTERN = Regex("""^\s+.+ -> .+$""")
    }
}
