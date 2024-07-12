package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType

internal class ProjectsLineConverter {

    private val projectRegex = Regex("^(.*)project (.*)$")
    private val functionalTypeRegexes = FunctionalType.entries.associateWith { it.asRegex() }

    fun convert(line: String): ProjectConvertedData? {
        val (prefix, modulePath) = projectRegex.find(line)?.destructured ?: return null

        val logicalModule = modulePath.substringBeforeLast(":")
        val moduleName = modulePath.substringAfterLast(":")

        val functionalType = functionalTypeRegexes.filterValues { it.matches(moduleName) }.keys.firstOrNull()

        val level = prefix.split(REPORT_DEPENDENCY_LEVEL_SEPARATOR_REGEX).size

        return ProjectConvertedData(
            modulePath = modulePath,
            logicalModule = logicalModule,
            functionalType = functionalType,
            level = level
        )
    }

    companion object {

        private val REPORT_DEPENDENCY_LEVEL_SEPARATOR_REGEX = "\\s{4}".toRegex()
    }
}
