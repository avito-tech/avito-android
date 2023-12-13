package com.avito.android.network_contracts

import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.codegen.MakeFilesExecutableTask
import com.avito.android.network_contracts.codegen.SetupTmpMtlsFilesTask
import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.network_contracts.extension.NetworkContractsRootExtension
import com.avito.android.network_contracts.internal.http.HttpClientService
import com.avito.android.network_contracts.scheme.imports.ApiSchemesImportTask
import com.avito.android.network_contracts.shared.findApiSchemes
import com.avito.android.network_contracts.shared.findPackageDirectory
import com.avito.android.network_contracts.shared.networkContractsExtension
import com.avito.android.network_contracts.shared.networkContractsRootExtension
import com.avito.android.network_contracts.shared.reportFile
import com.avito.android.network_contracts.snapshot.PrepareGeneratedCodeSnapshotTask
import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask
import com.avito.android.network_contracts.validation.ValidateNetworkContractsTask
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.withType
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class NetworkContractsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        createNetworkContractsExtension(target)

        if (target.isRoot()) {
            createCodegenConfigurations(target)
            configureMakeCodegenFileExecutableTask(target)
            configureSetupMtlsVariablesTask(target)
            configureVerificationRootTask(target)
        } else {
            configureAddEndpointTask(target)
            configureSnapshotTask(target)
            configureCodegenTask(target)
            configureValidationTask(target)
        }
    }

    private fun createNetworkContractsExtension(project: Project) {
        if (project.isRoot()) {
            project.extensions.create<NetworkContractsRootExtension>(NetworkContractsRootExtension.NAME)
        } else {
            project.extensions.create<NetworkContractsModuleExtension>(NetworkContractsModuleExtension.NAME)
        }
    }

    private fun createCodegenConfigurations(
        target: Project,
    ): Configuration {
        return target.configurations.create("codegen") {
            it.isTransitive = false
        }
    }

    private fun configureSnapshotTask(
        project: Project
    ) {
        val networkContractsExtension = project.networkContractsExtension

        project.tasks.register<PrepareGeneratedCodeSnapshotTask>(PrepareGeneratedCodeSnapshotTask.NAME) {
            val codegenSourcesDir = project
                .findPackageDirectory(networkContractsExtension.packageName)
                .map { it.dir("generated") }

            from(codegenSourcesDir.get().asFile.path) {
                include("**/*.kt")
            }

            outputDirectory.set(project.layout.buildDirectory.dir("prepareCodegenSnapshot"))
        }
    }

    private fun configureCodegenTask(
        target: Project,
    ) {
        val networkContractsExtension = target.networkContractsExtension
        val rootExtension = target.networkContractsRootExtension
        val setupMtlsTask = target.rootProject.tasks
            .named(
                SetupTmpMtlsFilesTask.NAME,
                SetupTmpMtlsFilesTask::class.java
            )

        val makeFilesExecutableTask = target.rootProject.tasks
            .named(
                MakeFilesExecutableTask.NAME,
                MakeFilesExecutableTask::class.java
            )

        val snapshotTask = target.tasks.typedNamed<Copy>("prepareCodegenSnapshot")
        target.tasks.register<CodegenTask>(CodegenTask.NAME) {
            val codegenSourcesDir = project.findPackageDirectory(networkContractsExtension.packageName)

            this.kind.set(networkContractsExtension.kind)
            this.projectName.set(networkContractsExtension.projectName)
            this.skipValidation.set(networkContractsExtension.skipValidation)
            this.schemes.setFrom(project.findApiSchemes())
            this.packageDirectory.set(codegenSourcesDir)
            this.outputDirectory.set(packageDirectory.map { it.dir("generated") })
            this.codegenBinaryFiles.setFrom(makeFilesExecutableTask.map { it.files })

            this.crtEnvName.set(rootExtension.crtEnvName)
            this.keyEnvName.set(rootExtension.keyEnvName)

            this.tmpCrtFile.set(setupMtlsTask.flatMap { it.tmpCrt })
            this.tmpKeyFile.set(setupMtlsTask.flatMap { it.tmpKey })

            this.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(this))
            this.snapshot.setFrom(snapshotTask.map { it.destinationDir })

            onlyIf { (it as? CodegenTask)?.schemes?.isEmpty == false }
        }
    }

    private fun configureAddEndpointTask(
        project: Project,
    ) {
        val httpClientService = HttpClientService.provideHttpClientService(project)
        project.tasks.register("addEndpoint", ApiSchemesImportTask::class.java) {
            if (!project.hasProperty("apiSchemesUrl")) {
                error(
                    "Parameter `apiSchemesUrl` is not specified. " +
                        "Run task with parameter `-PapiSchemesUrl` with desired path."
                )
            }
            it.apiPath.set(project.getMandatoryStringProperty("apiSchemesUrl"))
            it.outputDirectory.set(
                project
                    .findPackageDirectory(project.networkContractsExtension.packageName)
                    .map { it.dir("api-clients") }
            )

            it.httpClientBuilder.set(httpClientService)
            it.usesService(httpClientService)

            it.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(project))
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

    private fun configureValidationTask(
        project: Project
    ) {
        val rootTask = project.rootProject.tasks.withType<ValidateNetworkContractsRootTask>()
        val codegenTasks = project.tasks
            .typedNamed<CodegenTask>(CodegenTask.NAME)

        val snapshotTask = project.tasks
            .typedNamed<PrepareGeneratedCodeSnapshotTask>(PrepareGeneratedCodeSnapshotTask.NAME)

        project.tasks.register<ValidateNetworkContractsTask>(ValidateNetworkContractsTask.NAME) {
            // Enable validation regardless of the setting in the extension,
            // as we need to validate codegen schemas also.
            project.networkContractsExtension.skipValidation.set(false)

            this.generatedFilesDirectory.set(snapshotTask.flatMap { it.outputDirectory })
            this.referenceFilesDirectory.set(codegenTasks.flatMap { it.outputDirectory })

            resultFile.set(
                project.reportFile(
                    directory = "networkContracts",
                    reportFileName = "codegenValidationReport.json"
                )
            )

            rootTask.configureEach { it.reports.from(resultFile) }
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
}
