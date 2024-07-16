package com.avito.android.module_type.validation

import com.avito.android.module_type.ModuleTypesPlugin
import com.avito.android.module_type.validation.configurations.missings.implementations.MissingFakeModuleConfiguration
import com.avito.android.module_type.validation.configurations.missings.implementations.MissingImplementationDependencyConfiguration
import com.avito.android.module_type.validation.internal.hasModuleTypePlugin
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.diagnostics.ProjectReportTask
import org.gradle.kotlin.dsl.register

public class ModuleTypeValidationPlugin : Plugin<Project> {

    private val validationConfigurations = setOf(
        MissingImplementationDependencyConfiguration(),
        MissingFakeModuleConfiguration(),
    )

    override fun apply(target: Project) {
        if (!target.hasModuleTypePlugin()) {
            target.plugins.apply(ModuleTypesPlugin::class.java)
        }

        if (target.isRoot()) {
            configureRoot(target)
        } else {
            configureModule(target)
        }
    }

    private fun configureRoot(target: Project) {
        target.tasks.register<ProjectReportTask>(PROJECT_LIST_TASK_NAME) {
            outputFile = target.layout.buildDirectory.file("dependencies/project_list.txt").get().asFile
        }

        validationConfigurations.forEach { configuration ->
            configuration.configureRoot(target)
        }
    }

    private fun configureModule(target: Project) {
        target.createValidationExtension()

        validationConfigurations.forEach { configuration ->
            configuration.configureModule(target)
        }
    }

    private fun Project.createValidationExtension() {
        val moduleTypeExtension = moduleTypeExtension()
        moduleTypeExtension.extensions.create(
            "validation",
            ValidationExtension::class.java
        )
    }

    public companion object {
        internal const val PROJECT_LIST_TASK_NAME = "extractProjects"
    }
}
