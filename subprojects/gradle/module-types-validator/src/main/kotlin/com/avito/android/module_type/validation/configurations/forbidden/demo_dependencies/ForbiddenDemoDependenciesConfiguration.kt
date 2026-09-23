package com.avito.android.module_type.validation.configurations.forbidden.demo_dependencies

import com.avito.android.module_type.validation.configurations.DependencyClasspath
import com.avito.android.module_type.validation.configurations.ExtractProjectDependenciesTask
import com.avito.android.module_type.validation.configurations.ValidationConfiguration
import com.avito.android.module_type.validation.configurations.registerExtractProjectDependencies
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.android.module_type.validation.internal.validationExtension
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal class ForbiddenDemoDependenciesConfiguration : ValidationConfiguration {

    override fun configureRoot(project: Project) = Unit

    override fun configureModule(project: Project) {
        val moduleTypeExtension = project.moduleTypeExtension()
        val validationExtension = moduleTypeExtension.validationExtension()

        project.registerForbiddenDemoDependenciesTask(
            validationExtension.forbiddenDemoDependenciesExtension
        )
    }

    private fun Project.registerForbiddenDemoDependenciesTask(
        extension: ForbiddenDemoDependenciesExtension
    ) {
        val dependenciesTask = registerExtractDependenciesTask()

        tasks.register<ForbiddenDemoDependenciesTask>(
            ForbiddenDemoDependenciesTask.NAME
        ) {
            appModulePath.set(this@registerForbiddenDemoDependenciesTask.path)
            appModuleBuildFilePath.set(buildFile.toRelativeString(rootDir))
            allowedDependencies.set(extension.allowedDependencies)
            forbiddenDependenciesFile.set(extension.forbiddenDependenciesFile)
            appDependenciesFile.set(dependenciesTask.flatMap { it.output.asFile })
            outputStatusFile.set(validationReportFile())
        }
    }

    private fun Project.registerExtractDependenciesTask(): TaskProvider<ExtractProjectDependenciesTask> {
        return registerExtractProjectDependencies(
            taskName = "extractDemoDependenciesForValidation",
            reportFile = dependenciesFile(),
            classpath = DependencyClasspath.RUNTIME,
        )
    }
}

private fun Project.dependenciesFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "dependencies/demo_dependencies.txt"
    )
}

private fun Project.validationReportFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "report/forbidden_demo_dependencies_validation.txt"
    )
}
