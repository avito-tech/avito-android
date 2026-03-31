package com.avito.android.contracts.platform

import com.avito.android.contracts.platform.dependency.codegenDependencyConfiguration
import com.avito.android.contracts.platform.extension.ContractsRootExtension
import com.avito.android.contracts.platform.extension.configurations.network.isDefault
import com.avito.android.contracts.platform.extension.defaultNetwork
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.scheme.codegen.SetupTmpMtlsFilesTask
import com.avito.android.contracts.platform.scheme.fixation.UpdateRemoteApiSchemesTask
import com.avito.android.contracts.platform.scheme.validation.CompositeTask
import com.avito.android.contracts.platform.shared.reportFile
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class ContractsRootPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) {
            "SchemesContractsRootPlugin must be applied to root project"
        }

        val rootExtension = target.extensions.create<ContractsRootExtension>(ContractsRootExtension.NAME)
        rootExtension.networks.configureEach { configuration ->
            if (!configuration.isDefault) {
                val defaultNetwork = rootExtension.defaultNetwork
                configuration.serviceUrl.convention(defaultNetwork.serviceUrl)
                configuration.serviceName.convention(defaultNetwork.serviceName)
                configuration.useTls.convention(defaultNetwork.useTls)
                configuration.retries.convention(defaultNetwork.retries)
                configuration.timeouts.convention(defaultNetwork.timeouts)
                configuration.crtEnvName.convention(defaultNetwork.crtEnvName)
                configuration.keyEnvName.convention(defaultNetwork.keyEnvName)
            }
        }

        target.codegenDependencyConfiguration.setArtifactsExecutable()

        configureSetupMtlsVariablesTask(target)
        configureValidationRootTask(target)
        configureContractFixationTask(target, rootExtension)
    }

    private fun configureSetupMtlsVariablesTask(
        target: Project
    ) {
        target.tasks.register<SetupTmpMtlsFilesTask>(SetupTmpMtlsFilesTask.NAME) {
            val buildDirectory = project.layout.buildDirectory
            this.tmpCrt.set(buildDirectory.dir(SetupTmpMtlsFilesTask.NAME).map { it.file("tmp_mtls_crt.crt") })
            this.tmpKey.set(buildDirectory.dir(SetupTmpMtlsFilesTask.NAME).map { it.file("tmp_mtls_key.key") })
            this.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(this))
            this.tlsCredentialsService.set(TlsConfigurationPlugin.provideCredentialsService(project))
        }
    }

    private fun configureValidationRootTask(
        project: Project
    ) {
        project.tasks.register<CompositeTask>(ContractsTaskNamesBuilder.validationTask("all"))
    }

    private fun configureContractFixationTask(project: Project, rootExtension: ContractsRootExtension) {
        val upsertCompositeTask = project.tasks.register(
            ContractsTaskNamesBuilder.updateSchemesTask("all"),
            CompositeTask::class.java
        )

        rootExtension.fixations
            .all { variant ->
                val variantUpsertTask = project.tasks.register(
                    ContractsTaskNamesBuilder.updateSchemesTask(variant.name),
                    UpdateRemoteApiSchemesTask::class.java
                ) {
                    it.analyticsTrackerService.set(NetworkContractsAnalyticsService.provideService(project))
                    it.loggerFactory.set(GradleLoggerPlugin.getLoggerFactory(project))
                    it.upsertService.set(variant.upsertService)
                    it.outputFile.set(project.reportFile("networkContracts", "upsert.txt"))
                    it.variantName.set(variant.name)
                }

                upsertCompositeTask.configure {
                    it.reports.from(variantUpsertTask.flatMap { it.outputFile })
                }
            }
    }
}
