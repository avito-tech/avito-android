package com.avito.android.network_contracts

import com.avito.android.network_contracts.codegen.CodegenTask
import com.avito.android.network_contracts.codegen.MakeFilesExecutableTask
import com.avito.android.network_contracts.codegen.SetupTmpMtlsFilesTask
import com.avito.android.network_contracts.extension.NetworkContractsModuleExtension
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
import com.avito.capitalize
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.withType
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool

public class NetworkContractsPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target.isRoot() && !target.plugins.hasPlugin(NetworkContractsRootPlugin::class.java)) {
            target.plugins.apply(NetworkContractsRootPlugin::class.java)
        } else {
            configureModulePlugin(target)
        }
    }

    private fun configureModulePlugin(target: Project) {
        createNetworkContractsExtension(target)

        registerCodegenTask(CodegenTask.NAME, target)

        configureAddEndpointTask(target)
        configureValidationTask(target)
        configureCollectSchemesTask(target)
        configureGeneratedSources(target)
    }

    private fun createNetworkContractsExtension(project: Project) {
        project.extensions.create<NetworkContractsModuleExtension>(NetworkContractsModuleExtension.NAME).apply {
            generatedDirectory.convention(project.findPackageDirectory(packageName))
            apiSchemesDirectory.convention(project.findPackageDirectory(packageName))
        }
    }

    private fun registerCodegenTask(
        name: String,
        target: Project,
        forceValidation: Boolean = false
    ): TaskProvider<CodegenTask> {
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

        return target.tasks.register(name, CodegenTask::class.java) {
            it.packageName.set(networkContractsExtension.packageName)
            it.moduleName.set(it.project.path)
            it.kind.set(networkContractsExtension.kind)
            it.codegenProjectName.set(networkContractsExtension.projectName)
            it.skipValidation.set(networkContractsExtension.skipValidation.map { forceValidation || it })
            it.moduleDirectory.set(it.project.layout.projectDirectory)
            it.outputDirectory.set(networkContractsExtension.generatedDirectory)
            it.codegenExecutableFiles.setFrom(makeFilesExecutableTask.map { it.files })
            it.schemesDir.set(networkContractsExtension.apiSchemesDirectory)

            it.crtEnvName.set(rootExtension.crtEnvName)
            it.keyEnvName.set(rootExtension.keyEnvName)

            if (forceValidation) {
                it.tmpCrtFile.set(setupMtlsTask.flatMap { it.tmpCrt })
                it.tmpKeyFile.set(setupMtlsTask.flatMap { it.tmpKey })
            }

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

    private fun configureValidationTask(
        project: Project
    ) {
        val rootTask = project.rootProject.tasks
            .withType<ValidateNetworkContractsRootTask>()

        val codegenTasks = registerCodegenTask(
            name = "validate" + CodegenTask.NAME.capitalize(),
            target = project,
            forceValidation = true
        )

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

    private fun configureCollectSchemesTask(project: Project) {
        val extension = project.networkContractsExtension

        val updateApiSchemesTask = project.rootProject.tasks
            .typedNamed<UpdateRemoteApiSchemesTask>(UpdateRemoteApiSchemesTask.NAME)

        val collectApiSchemesTask = project.tasks.register<CollectApiSchemesTask>(CollectApiSchemesTask.NAME) {
            projectPath.set(project.path)
            projectName.set(extension.projectName)
            codegenTomlFile.set(
                project.layout.projectDirectory
                    .asFileTree
                    .matching { it.include("**/codegen.toml") }
                    .firstOrNull()
            )
            schemesDirectory.set(extension.apiSchemesDirectory.flatMap { it.dir(extension.schemesDirName) })

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
        val codegenTask = project.tasks.typedNamed<CodegenTask>(CodegenTask.NAME)

        project.tasks.withType<KotlinCompileTool>().configureEach {
            it.source(codegenTask.flatMap { it.outputDirectory })
        }
    }
}
