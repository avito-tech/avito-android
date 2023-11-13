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
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

public class NetworkContractsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        createNetworkContractsExtension(target)

        if (target.isRoot()) {
            createCodegenConfigurations(target)
            configureMakeCodegenFileExecutableTask(target)
            configureSetupMtlsVariablesTask(target)
        } else {
            configureAddEndpointTask(target)
            configureCodegenTask(target)
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

        target.tasks.register<CodegenTask>(CodegenTask.NAME) {
            val packagePath = networkContractsExtension.packageName.map { it.replace(".", File.separator) }
            val codegenSourcesDir = project.findPackageDirectory(packagePath)

            this.kind.set(networkContractsExtension.kind)
            this.projectName.set(networkContractsExtension.projectName)
            this.packagePath.set(packagePath)
            this.skipValidation.set(networkContractsExtension.skipValidation)
            this.schemes.setFrom(project.findApiSchemes())
            this.outputDirectory.set(codegenSourcesDir)
            this.codegenBinaryFiles.setFrom(makeFilesExecutableTask.map { it.files })

            this.crtEnvName.set(rootExtension.crtEnvName)
            this.keyEnvName.set(rootExtension.keyEnvName)

            this.tmpCrtFile.set(setupMtlsTask.flatMap { it.tmpCrt })
            this.tmpKeyFile.set(setupMtlsTask.flatMap { it.tmpKey })

            this.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(this))

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
}
