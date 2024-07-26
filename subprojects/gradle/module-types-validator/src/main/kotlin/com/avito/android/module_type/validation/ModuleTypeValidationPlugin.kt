package com.avito.android.module_type.validation

import com.avito.android.module_type.ModuleTypesPlugin
import com.avito.android.module_type.validation.configurations.missings.implementations.MissingImplementationDependencyConfiguration
import com.avito.android.module_type.validation.internal.hasModuleTypePlugin
import com.avito.android.module_type.validation.internal.moduleTypeExtension
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ModuleTypeValidationPlugin : Plugin<Project> {

    private val validationConfigurations = setOf(
        MissingImplementationDependencyConfiguration()
    )

    override fun apply(target: Project) {
        if (!target.hasModuleTypePlugin()) {
            target.plugins.apply(ModuleTypesPlugin::class.java)
        }

        validationConfigurations.forEach { configuration ->
            if (target.isRoot()) {
                configuration.configureRoot(target)
            } else {
                target.createValidationExtension()
                configuration.configureModule(target)
            }
        }
    }

    private fun Project.createValidationExtension() {
        val moduleTypeExtension = moduleTypeExtension()
        moduleTypeExtension.extensions.create(
            "validation",
            ValidationExtension::class.java
        )
    }
}
