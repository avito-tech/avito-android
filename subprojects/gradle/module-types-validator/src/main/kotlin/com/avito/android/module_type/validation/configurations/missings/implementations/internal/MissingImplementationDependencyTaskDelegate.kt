package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType

internal class MissingImplementationDependencyTaskDelegate {
    fun validate(
        appModulePath: String,
        appModuleBuildFilePath: String,
        appModuleType: FunctionalType,
        projectsTaskOutputText: String,
        appDependenciesText: String
    ): Result<Unit> {
        val allProjects = ProjectListFileReader(
            reportFileText = projectsTaskOutputText
        ).readProjectList()

        val appDependencies = DependenciesFileReader(
            appDependenciesText,
            appModulePath
        ).readProjectDependencies()

        val implementationTypes = setOf(FunctionalType.Impl, FunctionalType.Fake, FunctionalType.Debug)

        val publicWithoutImplementation = appDependencies
            .groupBy { it.logicalModule }
            .filterKeys { it.isNotEmpty() }
            .filterValues { modules -> modules.all { it.functionalType !in implementationTypes } }

        val dependencies = publicWithoutImplementation
            .filter { (logicalModule, _) ->
                findImplementationModules(allProjects, logicalModule, appModuleType).isNotEmpty()
            }
            .toSortedMap()

        if (dependencies.isEmpty()) {
            return Result.success(Unit)
        }

        val errorText = buildString {
            appendLine()
            appendLine("Please add the following dependencies to $appModuleBuildFilePath")

            appendLine()
            appendLine("    implementation(")

            dependencies.keys.forEach { logicalModule ->
                val implementationModules = findImplementationModules(
                    allProjects,
                    logicalModule,
                    appModuleType
                )
                val line = implementationModules.joinToString(prefix = "        ", separator = " // or ") {
                    "project(\"$it\"),"
                }
                appendLine(line)
            }

            appendLine("    )")

            dependencies.values.forEach {
                append(createDetailedInformation(it))
            }
        }
        return Result.failure(Exception(errorText))
    }

    private fun createDetailedInformation(
        availablePaths: List<ProjectDependencyInfo>,
    ): String {
        val pathsToPublicModules = availablePaths
            .filter { it.functionalType == FunctionalType.Public }
            .groupBy { it.modulePath }

        return buildString {
            pathsToPublicModules.forEach { (moduleName, paths) ->
                appendLine()
                appendLine("Dependency for $moduleName appears from: ")
                paths.forEach {
                    appendLine("    " + it.fullPath)
                }
            }
        }
    }

    private fun findImplementationModules(
        allProjects: List<String>,
        logicalModule: String,
        appModuleType: FunctionalType
    ): List<String> {
        val prefix = "$logicalModule:"

        val implementationTypeToAdd = when (appModuleType) {
            FunctionalType.UserApp -> FunctionalType.Impl
            FunctionalType.DemoApp -> FunctionalType.Fake
            else -> error("Unexpected appModuleType $appModuleType")
        }

        return allProjects
            .filter { it.startsWith(prefix) }
            .filter { it.removePrefix(prefix).matches(implementationTypeToAdd.asRegex()) }
    }
}
