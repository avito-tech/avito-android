package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType
import com.avito.capitalize

internal class ProjectsLineConverter {

    private val projectRegex = Regex("^(.*)project (.*)$")
    private val functionalTypeRegex = FunctionalType.values().asRegex()

    fun convert(line: String): ProjectConvertedData? {
        val matcher = projectRegex.find(line)
        if (matcher == null || matcher.groups.size <= 1) {
            return null
        }
        val groups = matcher.groupValues

        val level = groups[1].split(REPORT_DEPENDENCY_LEVEL_SEPARATOR_REGEX).size
        val path = groups[2]
        val logicalModule = path.substringBeforeLast(":")

        val functionalModuleGroups = functionalTypeRegex.find(path.substringAfterLast(":"))
            ?.groupValues
            .orEmpty()

        val functionalType = functionalModuleGroups.getOrNull(1)
            // In case when logical module can be like `impl-something`
            ?.substringBefore("-")

        return ProjectConvertedData(
            modulePath = path,
            logicalModule = logicalModule,
            functionalType = functionalType?.let { FunctionalType.valueOf(functionalType.capitalize()) },
            level = level
        )
    }

    companion object {

        private val REPORT_DEPENDENCY_LEVEL_SEPARATOR_REGEX = "\\s{4}".toRegex()
    }
}
