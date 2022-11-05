package com.avito.android.tech_budget.internal.warnings.log.converter

import com.avito.android.tech_budget.internal.warnings.log.LogEntry
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import java.io.File

internal class LogToWarningConverter {

    fun convert(log: LogEntry): Warning {
        val rawText = log.rawText
        val warningWithoutPrefix = rawText.removePrefix("w: ")
        val sourceFile = parseSourceFilePath(warningWithoutPrefix)

        return Warning(
            moduleName = log.projectInfo.path,
            ownerNames = log.projectInfo.owners,
            sourceFile = sourceFile,
            fullMessage = warningWithoutPrefix
        )
    }

    private fun parseSourceFilePath(warningWithoutPrefix: String): String? {
        if (!warningWithoutPrefix.startsWith("/")) return null
        val filePath = warningWithoutPrefix.takeWhile { it != ':' }
        if (!File(filePath).exists()) return null
        return filePath
    }
}
