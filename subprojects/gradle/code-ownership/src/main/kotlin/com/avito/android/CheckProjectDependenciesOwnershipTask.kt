package com.avito.android

import Visibility
import com.avito.impact.configuration.internalModule
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class CheckProjectDependenciesOwnershipTask : DefaultTask() {

    @TaskAction
    fun checkProjectRelations() {
        val moduleType = project.internalModule.project.extensions.moduleType?.type

        if (moduleType == ModuleType.COMPONENT_TEST) {
            componentTestLibraryChecks()
        } else {
            ordinaryLibraryChecks()
        }
    }

    private fun componentTestLibraryChecks() {
        checkUnitDependencyRuleDependency()
    }

    private fun ordinaryLibraryChecks() {
        checkPrivateDependenciesRule()
        checkUnitDependencyRuleDependency()
    }

    private fun checkPrivateDependenciesRule() {
        project.internalModule
            .implementationConfiguration
            .dependencies.forEach {
                val codeOwnershipExtensionTo = it.module.project.extensions.ownership
                if (codeOwnershipExtensionTo.visibility == Visibility.PRIVATE) {
                    throwInvalidDependencyOnPrivateVisibleModuleException(
                        project.path,
                        it.module.project.path,
                        codeOwnershipExtensionTo.team
                    )
                }
            }
    }

    private fun checkUnitDependencyRuleDependency() {
        val codeOwnershipExtensionFrom = project.extensions.ownership
        project.internalModule
            .implementationConfiguration
            .dependencies.forEach {
                val codeOwnershipExtensionTo = it.module.project.extensions.ownership
                if (codeOwnershipExtensionTo.visibility == Visibility.TEAM
                    && codeOwnershipExtensionFrom.team != codeOwnershipExtensionTo.team
                ) {
                    if (codeOwnershipExtensionFrom.allowedDependencies.contains(it.path)) {
                        logger.warn(
                            "WARNING: ${project.path} has forbidden dependency on ${it.path}, " +
                                "but was allowed explicitly in configuration"
                        )
                    } else {
                        throwInvalidDependencyOnUnitVisibleModuleException(
                            project.path,
                            it.module.project.path,
                            codeOwnershipExtensionTo.team
                        )
                    }
                }
            }
    }

    private fun throwInvalidDependencyOnPrivateVisibleModuleException(
        projectPath: String,
        dependentProjectPath: String,
        dependentProjectOwner: String?
    ) {
        throw IllegalStateException(
            "$projectPath has forbidden dependency on $dependentProjectPath. \n" +
                "Team $dependentProjectOwner marked $dependentProjectPath as ${Visibility.PRIVATE}-visible. \n" +
                "${Visibility.PRIVATE} modules are protected from using by another modules."
        )
    }

    private fun throwInvalidDependencyOnUnitVisibleModuleException(
        projectPath: String,
        dependentProjectPath: String,
        dependentProjectOwner: String?
    ) {
        throw IllegalStateException(
            "$projectPath has forbidden dependency on $dependentProjectPath. \n" +
                "Team $dependentProjectOwner marked $dependentProjectPath as ${Visibility.TEAM}-visible. \n" +
                "Only $dependentProjectOwner's modules can use ${Visibility.PRIVATE} modules."
        )
    }
}
