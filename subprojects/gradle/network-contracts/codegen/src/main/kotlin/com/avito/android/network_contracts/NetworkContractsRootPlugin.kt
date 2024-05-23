package com.avito.android.network_contracts

import com.avito.android.network_contracts.codegen.MakeFilesExecutableTask
import com.avito.android.network_contracts.codegen.SetupTmpMtlsFilesTask
import com.avito.android.network_contracts.extension.NetworkContractsRootExtension
import com.avito.android.network_contracts.internal.http.HttpClientService
import com.avito.android.network_contracts.scheme.fixation.upsert.UpdateRemoteApiSchemesTask
import com.avito.android.network_contracts.shared.reportFile
import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.git.gitStateProvider
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class NetworkContractsRootPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        check(target.isRoot()) {
            "NetworkContractsRootPlugin must be applied to root project"
        }

        target.extensions.create<NetworkContractsRootExtension>(NetworkContractsRootExtension.NAME)

        createCodegenConfigurations(target)
        configureMakeCodegenFileExecutableTask(target)
        configureSetupMtlsVariablesTask(target)
        configureVerificationRootTask(target)
        configureContractFixationTask(target)
    }

    private fun createCodegenConfigurations(
        target: Project,
    ): Configuration {
        return target.configurations.create("codegen") {
            it.isTransitive = false
        }
    }

    private fun configureMakeCodegenFileExecutableTask(
        target: Project,
    ): TaskProvider<MakeFilesExecutableTask> {
        val codegenConfiguration = target.rootProject.configurations.getByName("codegen")
        return target.tasks.register<MakeFilesExecutableTask>(MakeFilesExecutableTask.NAME) {
            this.files.setFrom(codegenConfiguration.files)
        }
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

    private fun configureVerificationRootTask(
        project: Project
    ) {
        project.tasks.register<ValidateNetworkContractsRootTask>(ValidateNetworkContractsRootTask.NAME) {
            this.rootDir.set(project.rootDir)
            this.projectPath.set(project.path)
            this.verdictFile.set(project.reportFile("networkContracts", "validation.txt"))
        }
    }

    private fun configureContractFixationTask(project: Project) {
        val validationTask = project.rootProject.tasks
            .typedNamed<ValidateNetworkContractsRootTask>(ValidateNetworkContractsRootTask.NAME)

        project.tasks.register(UpdateRemoteApiSchemesTask.NAME, UpdateRemoteApiSchemesTask::class.java) {
            it.httpClientService.set(HttpClientService.provideHttpClientService(project))
            it.author.set(project.getMandatoryStringProperty("avito.networkContracts.fixation.author"))
            it.branchName.set(project.gitStateProvider().map { it.currentBranch.name })
            it.loggerFactory.set(GradleLoggerPlugin.getLoggerFactory(project))
            it.validationReport.set(validationTask.flatMap { it.verdictFile })
        }
    }
}
