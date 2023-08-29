package com.avito.android.module_type.validation

import com.avito.android.module_type.DefaultModuleType
import com.avito.android.module_type.FunctionalType
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.module_type.ModuleTypesPlugin
import com.avito.android.module_type.validation.internal.hasModuleTypePlugin
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.android.module_type.validation.publicimpl.ValidatePublicDependenciesImplementedRootTask
import com.avito.android.module_type.validation.publicimpl.ValidatePublicDependenciesImplementedTask
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.withType
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

        if (target.isRoot()) {
            configureRootModule(target)
        } else {
            configureNonRootModule(target)
        }
    }

    private fun configureRootModule(project: Project) {
        project.tasks.register<ValidatePublicDependenciesImplementedRootTask>(
            ValidatePublicDependenciesImplementedTask.NAME
        ) {
            rootProjectDir.set(project.rootDir)
        }
    }

    private fun configureNonRootModule(target: Project) {
        val moduleTypeExtension = target.moduleTypeExtension()

        val validationExtension = moduleTypeExtension.extensions.create(
            "validation",
            DependenciesValidationExtension::class.java
        )

        target.registerPublicImplValidationTask(
            moduleTypeExtension,
            validationExtension.publicImplValidationExtension
        )
    }

    private fun Project.registerPublicImplValidationTask(
        moduleTypeExtension: ModuleTypeExtension,
        validationExtension: PublicImplValidationExtension
    ) {
        val project = this
        val projectConfigurations = this.configurations

        val dependenciesTask = tasks.register<DependencyReportTask>("extractDependencies") {
            val dependentMetadataConfigurations = validationExtension.configurationNames
                .get()
                .map { it + "DependenciesMetadata" }

            configurations = projectConfigurations
                .filter { it.name in dependentMetadataConfigurations }.toSet()

            outputFile = dependenciesFile().get().asFile
        }

        val moduleValidationTask = tasks.register<ValidatePublicDependenciesImplementedTask>(
            ValidatePublicDependenciesImplementedTask.NAME
        ) {
            val moduleType = moduleTypeExtension.type.get()

            require(moduleType is DefaultModuleType) {
                "${ValidatePublicDependenciesImplementedTask.NAME} cannot run for module type $moduleType"
            }

            require(moduleType.type == FunctionalType.Application) {
                "This validation check should be performed only on demo or application modules"
            }

            projectDependencies.set(dependenciesTask.map { requireNotNull(it.outputFile) })
            projectPath.set(project.path)
            rootProjectDir.set(rootProject.projectDir)
            buildFile.set(getBuildFile())
            reportFile.set(layout.buildDirectory.file("report/public_impl_validation.txt"))
        }

        rootProject.tasks.withType<ValidatePublicDependenciesImplementedRootTask>().configureEach {
            it.reports.from(moduleValidationTask.map { it.reportFile })
        }
    }
}

private fun Project.dependenciesFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "dependencies/project_dependencies.txt"
    )
}
