package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.validation.configurations.REPORT_INDENT

internal class ProjectsLineConverter {

    private val projectRegex = Regex("^((?:$REPORT_INDENT)*)(:\\S+)$")
    private val functionalTypeRegexes = FunctionalType.entries.associateWith { it.asRegex() }

    fun convert(line: String): ProjectConvertedData? {
        val (indent, modulePath) = projectRegex.find(line)?.destructured ?: return null

        val logicalModule = modulePath.substringBeforeLast(":")
        val moduleName = modulePath.substringAfterLast(":")

        val functionalType = functionalTypeRegexes.filterValues { it.matches(moduleName) }.keys.firstOrNull()

        val level = indent.length / REPORT_INDENT.length + 1

        return ProjectConvertedData(
            modulePath = modulePath,
            logicalModule = logicalModule,
            functionalType = functionalType,
            level = level
        )
    }
}
