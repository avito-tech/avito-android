package com.avito.android.module_type.validation.publicimpl.internal

import java.io.File

internal class DependenciesFileReader(
    private val reportFile: File,
    private val projectPath: String,
) {

    fun readProjectDependencies(): List<ProjectDependencyInfo> {
        val projectsLineConverter = ProjectsLineConverter()
        val projectPathBuilder = ProjectPathBuilder(projectPath)
        return reportFile.readLines().mapNotNull { line ->
            val convertedData = projectsLineConverter.convert(line) ?: return@mapNotNull null
            projectPathBuilder.calculateNextDependency(convertedData.modulePath, convertedData.level)
            ProjectDependencyInfo(
                modulePath = convertedData.modulePath,
                logicalModule = convertedData.logicalModule,
                functionalType = convertedData.functionalType,
                level = convertedData.level,
                fullPath = projectPathBuilder.path
            )
        }
    }
}
