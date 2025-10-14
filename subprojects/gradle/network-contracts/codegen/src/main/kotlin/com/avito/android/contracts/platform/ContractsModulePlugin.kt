package com.avito.android.contracts.platform

import com.avito.android.contracts.platform.dependency.codegenDependencyConfiguration
import com.avito.android.contracts.platform.extension.ContractsModuleExtension
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class ContractsModulePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(!target.isRoot()) {
            "NetworkContractsPlugin should not be applied to root"
        }

        val contractsExtension = target.extensions.create<ContractsModuleExtension>(ContractsModuleExtension.NAME)

        target.codegenDependencyConfiguration.setArtifactsExecutable()

        val pluginInstaller = ContractsPluginInstaller(target)

        pluginInstaller.installModule(target, contractsExtension)

        contractsExtension.validations.all { pluginInstaller.installValidations(it) }
        contractsExtension.imports.all { pluginInstaller.installCollectSchemesTask(it, contractsExtension) }
    }
}
