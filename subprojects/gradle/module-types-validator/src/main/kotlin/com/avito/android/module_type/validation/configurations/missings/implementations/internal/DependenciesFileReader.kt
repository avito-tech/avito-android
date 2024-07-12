package com.avito.android.module_type.validation.configurations.missings.implementations.internal

internal class DependenciesFileReader(
    private val reportFileText: String,
    private val projectPath: String,
) {

    fun readProjectDependencies(): List<ProjectDependencyInfo> {
        val projectsLineConverter = ProjectsLineConverter()
        val projectPathBuilder = ProjectPathBuilder(projectPath)
        return reportFileText.lines().mapNotNull { line ->
            val convertedData = projectsLineConverter.convert(line) ?: return@mapNotNull null
            projectPathBuilder.calculateNextDependency(convertedData.modulePath, convertedData.level)
            ProjectDependencyInfo(
                modulePath = convertedData.modulePath,
                logicalModule = convertedData.logicalModule,
                functionalType = convertedData.functionalType,
                fullPath = projectPathBuilder.path
            )
        }
    }
}
