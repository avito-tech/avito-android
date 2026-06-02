package com.avito.android.network_contracts

import com.avito.android.contracts.platform.ContractsModulePlugin
import com.avito.android.contracts.platform.ContractsTaskNamesBuilder
import com.avito.android.contracts.platform.extension.ContractsModuleExtension
import com.avito.android.contracts.platform.extension.codegen
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.internal.http.HttpClientService
import com.avito.android.contracts.platform.scheme.collect.CollectApiSchemesTask
import com.avito.android.contracts.platform.scheme.imports.data.NetworkSchemesImportService
import com.avito.android.contracts.platform.scheme.imports.data.ParamType
import com.avito.android.contracts.platform.scheme.imports.data.SchemesImportService
import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.EmptyCodegenTomlFileDiagnosticRule
import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.EmptySchemesDiagnosticRule
import com.avito.android.contracts.platform.scheme.validation.analyzer.rules.RemoteCompatibilityDiagnosticRule
import com.avito.android.network_contracts.NetworkContractsModulePlugin.Companion.VARIANT_NAME
import com.avito.android.network_contracts.validation.service.ValidationApiSchemesServiceImpl
import com.avito.git.gitStateProvider
import com.avito.kotlin.dsl.toOptional
import com.avito.kotlin.dsl.withType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.kotlin.dsl.getByType

public class NetworkContractsModulePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(ContractsModulePlugin::class.java)) {
            target.plugins.apply(ContractsModulePlugin::class.java)
        }

        val schemesContractsExtension = target.extensions.getByType<ContractsModuleExtension>()
        val networkContractsExtension =
            target.extensions.create("networkContracts", NetworkContractsModuleExtension::class.java)

        schemesContractsExtension.kind.set(networkContractsExtension.kind)
        schemesContractsExtension.projectName.set(networkContractsExtension.projectName)
        schemesContractsExtension.schemesBaseDirectory.set(networkContractsExtension.apiSchemesDirectory)

        schemesContractsExtension.codegen {
            it.packageName.set(networkContractsExtension.packageName)
            it.apiClassName.set(networkContractsExtension.apiClassName)
            it.version.set(networkContractsExtension.version)
            it.flags.set(networkContractsExtension.flags)
            it.mappings.set(networkContractsExtension.mappings)
            it.skipValidation.set(networkContractsExtension.skipValidation)
            it.codegenTimeoutSeconds.set(networkContractsExtension.codegenTimeoutSeconds)
            it.generatedDirectory.set(networkContractsExtension.generatedDirectory)
            it.errorOutputType.set(networkContractsExtension.errorOutputType)
            it.packageName.set(networkContractsExtension.packageName)
            it.generators.add("api-composition-clients")
        }

        schemesContractsExtension.imports.register(NetworkContractsVariantConstants.NAME) { extension ->
            extension.schemesDirName.set(networkContractsExtension.schemesDirName)

            val importService = SchemesImportService.provideImportService(
                target,
                NetworkSchemesImportService::class.java
            ) {
                it.httpClient.set(
                    target.provider {
                        HttpClientService.provideHttpClientService(target, extension.name)
                    }
                )
                @Suppress("DEPRECATION")
                it.additionalParams.putAll(
                    networkContractsExtension.importParameters.map {
                        it.mapValues { entry -> ParamType.Primitive(entry.value) }
                    }
                )
                it.additionalParams.putAll(networkContractsExtension.importTypedParameters)
            }
            extension.importService.set(importService)
        }

        target.configureNetworkContractsValidationTasks(schemesContractsExtension)
    }

    internal companion object {

        const val VARIANT_NAME = "network"
    }
}

private fun Project.configureNetworkContractsValidationTasks(
    extension: ContractsModuleExtension,
) {
    val rawSchemes = extension.schemesBaseDirectory.map { it.asFileTree }
    val objects = project.objects
    val codegenTomlFile = objects.fileProperty()
        .convention(project.layout.projectDirectory.file(project.provider { "codegen.toml" }))
        .toOptional()

    extension.validations.create(VARIANT_NAME) { variantConfiguration ->
        val collectApiSchemesTask = project.tasks.withType<CollectApiSchemesTask>()
            .named(ContractsTaskNamesBuilder.collectSchemesTask(variantConfiguration.name))

        val httpClient = HttpClientService.provideHttpClientService(project, variantConfiguration.name)

        val localValidations = variantConfiguration
            .rulesGroups
            .register("local")

        localValidations.configure { configuration ->
            configuration.registerRule("emptyCodegenToml", EmptyCodegenTomlFileDiagnosticRule::class.java) {
                it.modulePath.set(project.path)
                it.codegenTomlFile.set(codegenTomlFile)
            }
            configuration.registerRule("emptySchemes", EmptySchemesDiagnosticRule::class.java) {
                it.modulePath.set(project.path)
                it.schemes.from(rawSchemes)
            }
        }
        val remoteValidations = variantConfiguration
            .rulesGroups
            .register("remote")

        remoteValidations.configure { configuration ->
            // Run rule only if validationByCodegen is false and the schemes are not empty
            val schemesMetadata = rawSchemes
                .zip(codegenTomlFile) { rawSchemes, codegenTomlFile ->
                    val fromCollectTask = rawSchemes.files.isNotEmpty()
                        && codegenTomlFile?.asFile?.exists() == true

                    if (fromCollectTask) {
                        collectApiSchemesTask.map { it.jsonSchemeMetadataFile }
                    } else {
                        Providers.notDefined()
                    }
                }
                .flatMap { it }

            configuration.dependsOn.add("local")

            configuration.registerRule("remote", RemoteCompatibilityDiagnosticRule::class.java) {
                it.validationService.set(httpClient.map { ValidationApiSchemesServiceImpl(it.buildClient()) })

                it.analyticsTrackerService.set(NetworkContractsAnalyticsService.provideService(project))

                if (schemesMetadata.isPresent) {
                    it.schemes.setFrom(schemesMetadata)
                }
                it.modulePath.set(project.path)
                it.kind.set(extension.kind)
                it.variantName.set(variantConfiguration.name)
                it.branch.set(project.gitStateProvider().map { it.currentBranch.name })
            }
            configuration.onlyIf.set(project.provider { schemesMetadata.isPresent })
        }
    }
}
