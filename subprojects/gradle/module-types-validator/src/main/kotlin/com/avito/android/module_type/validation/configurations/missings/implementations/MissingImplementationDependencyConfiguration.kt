package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.DefaultModuleType
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.module_type.validation.configurations.ValidationConfiguration
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.android.module_type.validation.internal.validationExtension
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.register

internal class MissingImplementationDependencyConfiguration : ValidationConfiguration {

    override fun configureRoot(project: Project) {
        project.tasks.register<MissingImplementationDependencyRootTask>(
            MissingImplementationDependencyTask.NAME
        ) {
            rootProjectDir.set(project.rootDir)
            outputFile.set(project.validationReportFile())
        }
    }

    override fun configureModule(project: Project) {
        val moduleTypeExtension = project.moduleTypeExtension()
        val validationExtension = moduleTypeExtension.validationExtension()

        project.registerMissingImplementationsTask(
            moduleTypeExtension,
            validationExtension.missingImplementationExtension
        )
    }

    private fun Project.registerMissingImplementationsTask(
        moduleTypeExtension: ModuleTypeExtension,
        missingImplementationExtension: MissingImplementationDependencyExtension
    ) {
        val dependenciesTask = registerExtractDependenciesTask(missingImplementationExtension)

        val moduleValidationTask = tasks.register<MissingImplementationDependencyTask>(
            MissingImplementationDependencyTask.NAME
        ) {
            val moduleType = moduleTypeExtension.type.get()

            require(moduleType is DefaultModuleType) {
                "${MissingImplementationDependencyTask.NAME} cannot run for module type $moduleType"
            }

            require(moduleType.type == FunctionalType.Application) {
                "This validation check should be performed only on demo or application modules"
            }

            projectDependencies.set(dependenciesTask.map { requireNotNull(it.outputFile) })
            projectPath.set(this@registerMissingImplementationsTask.path)
            buildFileRelativePath.set(buildFile.toRelativeString(rootDir))
            reportFile.set(validationReportFile())
        }

        rootProject.tasks.withType<MissingImplementationDependencyRootTask>().configureEach {
            it.reports.from(moduleValidationTask.map { it.reportFile })
        }
    }

    private fun Project.registerExtractDependenciesTask(
        extension: MissingImplementationDependencyExtension
    ): TaskProvider<DependencyReportTask> {
        return tasks.register<DependencyReportTask>("extractDependencies") {
            val dependentMetadataConfigurations = extension.configurationNames
                .get()
                .map { it + "DependenciesMetadata" }

            configurations = project.configurations
                .filter { it.name in dependentMetadataConfigurations }
                .toSet()

            outputFile = dependenciesFile().get().asFile
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
