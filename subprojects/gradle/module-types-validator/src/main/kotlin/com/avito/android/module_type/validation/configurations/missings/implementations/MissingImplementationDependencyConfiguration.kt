package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.module_type.validation.configurations.DependencyClasspath
import com.avito.android.module_type.validation.configurations.ValidationConfiguration
import com.avito.android.module_type.validation.configurations.registerExtractProjectDependencies
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.android.module_type.validation.internal.projectListTaskOutput
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.register
import java.io.File

internal class MissingImplementationDependencyConfiguration : ValidationConfiguration {

    override fun configureRoot(project: Project) {
        project.tasks.register<MissingImplementationDependencyRootTask>(
            MissingImplementationDependencyTask.NAME
        ) {
            outputFile.set(project.validationReportFile())
        }
    }

    override fun configureModule(project: Project) {
        val moduleTypeExtension = project.moduleTypeExtension()

        project.registerMissingImplementationsTask(
            moduleTypeExtension,
            project.projectListTaskOutput(),
        )
    }

    private fun Project.registerMissingImplementationsTask(
        moduleTypeExtension: ModuleTypeExtension,
        projectsTaskOutput: Provider<File>
    ) {
        val dependenciesTask = registerExtractProjectDependencies(
            taskName = "extractDependencies",
            reportFile = dependenciesFile(),
            classpath = DependencyClasspath.COMPILE,
        )

        val moduleValidationTask = tasks.register<MissingImplementationDependencyTask>(
            MissingImplementationDependencyTask.NAME
        ) {
            val moduleType = moduleTypeExtension.type.get()

            require(moduleType.type == FunctionalType.DemoApp || moduleType.type == FunctionalType.UserApp) {
                "This validation check should be performed only on demo or application modules"
            }

            appModulePath.set(this@registerMissingImplementationsTask.path)
            appModuleBuildFilePath.set(buildFile.toRelativeString(rootDir))
            this.projectsTaskOutput.set(projectsTaskOutput)
            appModuleType.set(moduleType.type)
            appDependencies.set(dependenciesTask.flatMap { it.output.asFile })
            outputStatusFile.set(validationReportFile())
            outputErrorMessageFile.set(validationErrorFile())
        }

        rootProject.tasks.withType<MissingImplementationDependencyRootTask>().configureEach {
            it.errorMessages.from(moduleValidationTask.map { it.outputErrorMessageFile })
        }
    }
}

private fun Project.dependenciesFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "dependencies/project_dependencies.txt"
    )
}

private fun Project.validationReportFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "report/public_impl_validation.txt"
    )
}

private fun Project.validationErrorFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "report/public_impl_validation_error.txt"
    )
}
