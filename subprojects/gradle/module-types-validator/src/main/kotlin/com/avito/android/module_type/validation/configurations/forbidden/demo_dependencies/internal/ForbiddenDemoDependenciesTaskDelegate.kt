package com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies.internal

import com.avito.android.module_type.validation.configurations.missings.implementations.internal.DependenciesFileReader

internal class ForbiddenDemoDependenciesTaskDelegate {

    fun validate(
        appModulePath: String,
        appModuleBuildFilePath: String,
        appDependenciesText: String,
        forbiddenDependenciesText: String,
        allowedDependencies: Set<String>,
    ): Result<Unit> {
        val forbiddenDependencies = forbiddenDependenciesText
            .lines()
            .map(String::trim)
            .toSet()

        val appDependenciesInfo = DependenciesFileReader(
            reportFileText = appDependenciesText,
            projectPath = appModulePath
        ).readProjectDependencies()

        val appDependencies = appDependenciesInfo
            .map { it.modulePath }
            .toSet()

        val forbiddenAppDependencies = appDependencies intersect forbiddenDependencies
        val notAllowedForbiddenAppDependencies = forbiddenAppDependencies - allowedDependencies
        val unusedAllowedDependencies = allowedDependencies - forbiddenAppDependencies

        if (notAllowedForbiddenAppDependencies.isEmpty() && unusedAllowedDependencies.isEmpty()) {
            return Result.success(Unit)
        }

        val errorText = buildString {
            if (notAllowedForbiddenAppDependencies.isNotEmpty()) {
                appendLine("$appModulePath depends on forbidden modules.")
                appendLine()
                appendLine("See docs for details: https://links.k.avito.ru/android-forbidden-demo-dependencies")
                appendLine()
                appendLine("Forbidden modules:")
                notAllowedForbiddenAppDependencies.forEach { dependency ->
                    appendLine("    $dependency")
                }
                notAllowedForbiddenAppDependencies.forEach { dependency ->
                    appendLine()
                    appendLine("Dependency for $dependency appears from:")
                    appDependenciesInfo
                        .filter { it.modulePath == dependency }
                        .map { it.fullPath }
                        .sorted()
                        .forEach { path ->
                            appendLine("    $path")
                        }
                }
            }

            if (unusedAllowedDependencies.isNotEmpty()) {
                if (isNotEmpty()) {
                    appendLine()
                    appendLine()
                }
                appendLine("Some allow() entries in $appModuleBuildFilePath are excessive. Remove them:")
                unusedAllowedDependencies
                    .sorted()
                    .forEach { dependency ->
                        appendLine("    allow(\"$dependency\")")
                    }
            }
        }

        return Result.failure(IllegalStateException(errorText))
    }
}
