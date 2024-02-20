package com.avito.android.network_contracts

import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.codegen.MakeFilesExecutableTask
import com.avito.android.network_contracts.codegen.SetupTmpMtlsFilesTask
import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
import com.avito.android.network_contracts.extension.NetworkContractsRootExtension
import com.avito.android.network_contracts.internal.http.HttpClientService
import com.avito.android.network_contracts.scheme.fixation.collect.CollectApiSchemesTask
import com.avito.android.network_contracts.scheme.fixation.upsert.UpdateRemoteApiSchemesTask
import com.avito.android.network_contracts.scheme.imports.ApiSchemesImportTask
import com.avito.android.network_contracts.shared.findPackageDirectory
import com.avito.android.network_contracts.shared.networkContractsExtension
import com.avito.android.network_contracts.shared.networkContractsRootExtension
import com.avito.android.network_contracts.shared.reportFile
import com.avito.android.network_contracts.validation.ValidateNetworkContractsRootTask
import com.avito.android.network_contracts.validation.ValidateNetworkContractsSchemesTask
import com.avito.android.tls.TlsConfigurationPlugin
import com.avito.git.gitState
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.withType
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool

public class NetworkContractsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        createNetworkContractsExtension(target)

        if (target.isRoot()) {
            createCodegenConfigurations(target)
            configureMakeCodegenFileExecutableTask(target)
            configureSetupMtlsVariablesTask(target)
            configureVerificationRootTask(target)
            configureContractFixationTask(target)
        } else {
            configureAddEndpointTask(target)
            configureCodegenTask(target)
            configureValidationTask(target)
            configureCollectSchemesTask(target)
            configureGeneratedSources(target)
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

        target.tasks.register(CodegenTask.NAME, CodegenTask::class.java) {
            it.packageName.set(networkContractsExtension.packageName)
            it.moduleName.set(it.project.path)
            it.kind.set(networkContractsExtension.kind)
            it.codegenProjectName.set(networkContractsExtension.projectName)
            it.skipValidation.set(networkContractsExtension.skipValidation)
            it.moduleDirectory.set(it.project.layout.projectDirectory)
            it.outputDirectory.set(networkContractsExtension.generatedDirectory)
            it.codegenExecutableFiles.setFrom(makeFilesExecutableTask.map { it.files })
            it.schemesDir.set(networkContractsExtension.apiSchemesDirectory)

            it.crtEnvName.set(rootExtension.crtEnvName)
            it.keyEnvName.set(rootExtension.keyEnvName)

            it.tmpCrtFile.set(setupMtlsTask.flatMap { it.tmpCrt })
            it.tmpKeyFile.set(setupMtlsTask.flatMap { it.tmpKey })

            it.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(it))

            it.onlyIf { (it as? CodegenTask)?.schemesDir?.get()?.asFileTree?.isEmpty == false }
        }
    }

    private fun configureAddEndpointTask(
        project: Project,
    ) {
        val httpClientService = HttpClientService.provideHttpClientService(project)
        val networkContractsModuleExtension = project.networkContractsExtension
        project.tasks.register(ApiSchemesImportTask.NAME, ApiSchemesImportTask::class.java) {
            it.apiPath.set(project.getOptionalStringProperty("apiSchemesUrl", ""))
            it.outputDirectory.set(
                networkContractsModuleExtension
                    .apiSchemesDirectory
                    .flatMap { it.dir(networkContractsModuleExtension.schemesDirName) }
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
        val rootTask = project.rootProject.tasks
            .withType<ValidateNetworkContractsRootTask>()

        val codegenTasks = project.tasks
            .typedNamed<CodegenTask>(CodegenTask.NAME)

        val validateSchemesTask = project.tasks
            .register<ValidateNetworkContractsSchemesTask>(ValidateNetworkContractsSchemesTask.NAME) {
                this.projectPath.set(project.path)
                this.schemes.from(codegenTasks.map { it.schemesDir })

                resultFile.set(
                    project.reportFile(
                        directory = "networkContracts",
                        reportFileName = "codegenValidationSchemesReport.json"
                    )
                )
            }

        rootTask.configureEach {
            it.reports.from(validateSchemesTask.map { it.resultFile })
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
            it.branchName.set(project.gitState().get().currentBranch.name)
            it.loggerFactory.set(GradleLoggerPlugin.getLoggerFactory(project))
            it.validationReport.set(validationTask.flatMap { it.verdictFile })
        }
    }

    private fun configureCollectSchemesTask(project: Project) {
        val extension = project.networkContractsExtension

        val updateApiSchemesTask = project.rootProject.tasks
            .typedNamed<UpdateRemoteApiSchemesTask>(UpdateRemoteApiSchemesTask.NAME)

        val collectApiSchemesTask = project.tasks.register<CollectApiSchemesTask>(CollectApiSchemesTask.NAME) {
            projectPath.set(project.path)
            projectName.set(extension.projectName)
            codegenTomlFile.set(codegenDirectoryProvider.map { it.file("codegen.toml") })
            schemesDirectory.set(codegenDirectoryProvider.flatMap { it.dir(extension.schemesDirName) })

            jsonSchemeMetadataFile.set(
                project.reportFile(
                    directory = "networkContracts",
                    reportFileName = "${CollectApiSchemesTask.NAME}.json"
                )
            )
        }

        updateApiSchemesTask.configure {
            it.schemes.from(collectApiSchemesTask.flatMap(CollectApiSchemesTask::jsonSchemeMetadataFile))
        }
    }

    private fun configureGeneratedSources(project: Project) {
        if (!project.getBooleanProperty(COMPILE_GENERATED_SOURCES_KEY)) {
            return
        }

        val codegenTask = project.tasks.typedNamed<CodegenTask>(CodegenTask.NAME)

        project.tasks.withType<KotlinCompileTool>().configureEach {
            it.source(codegenTask.flatMap { it.outputDirectory })
        }
    }

    public companion object {

        /**
         * Flag to enable or disable the compilation of generated sources.
         *
         * Used for including files in the compilation that are generated by tasks into separate directories.
         */
        public const val COMPILE_GENERATED_SOURCES_KEY: String = "networkContracts.generated.compile"
    }
}
