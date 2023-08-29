package com.avito.android.module_type.validation

import com.avito.android.module_type.DefaultModuleType
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.module_type.ModuleTypesPlugin
import com.avito.android.module_type.validation.internal.hasModuleTypePlugin
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.android.module_type.validation.publicimpl.ValidatePublicDependenciesImplementedTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.diagnostics.DependencyReportTask
import org.gradle.kotlin.dsl.register

public class ModuleTypeValidationPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (!target.hasModuleTypePlugin()) {
            target.plugins.apply(ModuleTypesPlugin::class.java)
        }

        val moduleTypeExtension = target.moduleTypeExtension()
        target.configurePublicImplDependenciesValidation(moduleTypeExtension)
    }

    private fun Project.configurePublicImplDependenciesValidation(extension: ModuleTypeExtension) {
        val validationExtension = extension.extensions.create(
            "validation",
            DependenciesValidationExtension::class.java
        )

        registerPublicImplValidationTask(extension, validationExtension.publicImplValidationExtension)
    }

    private fun Project.registerPublicImplValidationTask(
        moduleTypeExtension: ModuleTypeExtension,
        validationExtension: PublicImplValidationExtension
    ) {
        val projectConfigurations = this.configurations

        val dependenciesTask = tasks.register<DependencyReportTask>("extractDependencies") {
            val dependentMetadataConfigurations = validationExtension.configurationNames
                .get()
                .map { it + "DependenciesMetadata" }

            configurations = projectConfigurations
                .filter { it.name in dependentMetadataConfigurations }.toSet()

            outputFile = dependenciesFile().get().asFile
        }

        tasks.register<ValidatePublicDependenciesImplementedTask>(
            ValidatePublicDependenciesImplementedTask.NAME
        ) {
            val moduleType = moduleTypeExtension.type.get()
            require(moduleType is DefaultModuleType) {
                "${ValidatePublicDependenciesImplementedTask.NAME} cannot run for module type $moduleType"
            }
            functionalType.set(moduleType.type)
            projectDependencies.set(dependenciesTask.map { requireNotNull(it.outputFile) })
            projectPath.set(path)
            rootProjectDir.set(rootProject.projectDir)
            reportFile.set(layout.buildDirectory.file("report/public_impl_validation.txt"))
        }
    }
}

private fun Project.dependenciesFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "dependencies/project_dependencies.txt"
    )
}
