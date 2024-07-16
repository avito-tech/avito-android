package com.avito.android.module_type.validation.configurations.missings.implementations.internal

import com.avito.android.module_type.FunctionalType
import java.util.SortedSet

internal class MissingFakeModuleRootTaskDelegate {
    fun validate(
        projectsTaskOutputText: String,
        ignoreLogicalModuleRegexesText: String,
    ): Result<Unit> {
        val allProjects = ProjectListFileReader(
            reportFileText = projectsTaskOutputText
        ).readProjectList()

        val ignoreLogicalModuleRegexes = ignoreLogicalModuleRegexesText.lines().map(::Regex)

        val logicalModulesWithPublic = getLogicalModulesWithModule(allProjects, FunctionalType.Public)
        val logicalModulesWithImpl = getLogicalModulesWithModule(allProjects, FunctionalType.Impl)
        val logicalModulesWithFake = getLogicalModulesWithModule(allProjects, FunctionalType.Fake)

        val incorrectLogicalModules =
            logicalModulesWithPublic.intersect(logicalModulesWithImpl) - logicalModulesWithFake

        val logicalModulesToReport = incorrectLogicalModules.filterNot { logicalModule ->
            ignoreLogicalModuleRegexes.any { it.matches(logicalModule) }
        }

        return if (logicalModulesToReport.isEmpty()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(
                """
                    The following logical modules have :public and :impl modules,
                    but do not have :fake modules. Create :fake modules for them.
                    See docs for details: https://links.k.avito.ru/android-missing-fake-modules
                    
                    
                """.trimIndent() + logicalModulesToReport.joinToString("\n")
            ))
        }
    }

    private fun getLogicalModulesWithModule(
        projects: List<String>,
        moduleType: FunctionalType
    ): SortedSet<String> {
        val moduleTypeRegex = moduleType.asRegex()
        return projects
            .filter { it.substringAfterLast(':').matches(moduleTypeRegex) }
            .map { it.substringBeforeLast(':') }
            .toSortedSet()
    }
}
