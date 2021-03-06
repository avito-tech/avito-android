package com.avito.android.build_checks.internal

import com.avito.android.build_checks.RootProjectChecksExtension
import com.google.common.annotations.VisibleForTesting
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.tasks.TaskAction

internal abstract class DynamicDependenciesTask : DefaultTask() {

    @TaskAction
    fun check() {
        project.subprojects.forEach {
            validateModule(it)
        }
    }

    private fun validateModule(project: Project) {
        project.configurations
            .flatMap { it.dependencies }
            .filterIsInstance<ExternalDependency>()
            .forEach { dep ->
                validateDependency(project, dep)
            }
    }

    private fun validateDependency(module: Project, dependency: ExternalDependency) {
        @Suppress("UnstableApiUsage")
        val version = dependency.versionConstraint.displayName

        check(!isDynamicVersion(version)) {
            FailedCheckMessage(
                RootProjectChecksExtension::dynamicDependencies,
                """
            Module ${module.path} has dynamic dependency ${dependency.name}:$version.
            It leads to non-reproducible builds and slower configuration time.
            Please use exact version.
            """
            ).toString()
        }
    }
}

/**
 * https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/VersionConstraint.html
 */
@VisibleForTesting
internal fun isDynamicVersion(version: String): Boolean {
    return when {
        version.endsWith('+') -> true
        version.startsWith('[') -> true
        version.startsWith(']') -> true
        version.startsWith('(') -> true
        version.startsWith("latest.") -> true
        else -> false
    }
}
