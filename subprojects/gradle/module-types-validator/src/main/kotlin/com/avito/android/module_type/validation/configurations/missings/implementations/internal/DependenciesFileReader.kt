package com.avito.android.module_type.validation.configurations.missings.implementations.internal

internal class DependenciesFileReader(
    private val reportFileText: String,
    private val projectPath: String,
) {

    fun readDependenciesByConfiguration(): List<List<ProjectDependencyInfo>> {
        return reportFileText.split("\n\n")
            .map { block -> readProjectDependencies(block.lines().drop(1)) }
            .filter { it.isNotEmpty() }
    }

    fun readProjectDependencies(): List<ProjectDependencyInfo> {
        return readDependenciesByConfiguration().flatten()
    }

    private fun readProjectDependencies(lines: List<String>): List<ProjectDependencyInfo> {
        val projectsLineConverter = ProjectsLineConverter()
        val projectPathBuilder = ProjectPathBuilder(projectPath)
        return lines.mapNotNull { line ->
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
