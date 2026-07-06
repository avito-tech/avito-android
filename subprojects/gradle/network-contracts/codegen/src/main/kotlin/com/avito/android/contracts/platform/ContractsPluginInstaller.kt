package com.avito.android.contracts.platform

import com.avito.android.contracts.platform.dependency.codegenDependencyConfiguration
import com.avito.android.contracts.platform.extension.ContractsModuleExtension
import com.avito.android.contracts.platform.extension.ContractsRootExtension
import com.avito.android.contracts.platform.extension.configurations.codegen.CodegenConfiguration
import com.avito.android.contracts.platform.extension.configurations.import.ImportConfiguration
import com.avito.android.contracts.platform.extension.configurations.validation.ValidationConfiguration
import com.avito.android.contracts.platform.extension.defaultNetwork
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.output.OutputTransformer
import com.avito.android.contracts.platform.output.OutputType
import com.avito.android.contracts.platform.output.parsers.CodegenJsonOutputTransformer
import com.avito.android.contracts.platform.scheme.codegen.CodegenTask
import com.avito.android.contracts.platform.scheme.codegen.SetupTmpMtlsFilesTask
import com.avito.android.contracts.platform.scheme.collect.CollectApiSchemesTask
import com.avito.android.contracts.platform.scheme.fixation.UpdateRemoteApiSchemesTask
import com.avito.android.contracts.platform.scheme.imports.ApiSchemesImportTask
import com.avito.android.contracts.platform.scheme.validation.CompositeTask
import com.avito.android.contracts.platform.scheme.validation.ValidateNetworkContractsTask
import com.avito.android.contracts.platform.shared.reportFile
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.kotlin.dsl.withType
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaCompilation
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class ContractsPluginInstaller(
    private val project: Project,
) {

    fun installModule(project: Project, extension: ContractsModuleExtension) {
        extension.codegen.outputTransformers.register(OutputType.TEXT.kind) {
            it.transformer.set(OutputTransformer { it })
        }
        extension.codegen.outputTransformers.register(OutputType.JSON.kind) {
            it.transformer.set(CodegenJsonOutputTransformer())
        }

        extension.validations.all { installValidations(it, extension) }
        extension.imports.all { installCollectSchemesTask(it, extension) }

        project.plugins.withType<KotlinBasePlugin> {
            registerCodegenVariantsTask(extension)
        }
    }

    private fun installCollectSchemesTask(configuration: ImportConfiguration, extension: ContractsModuleExtension) {
        configureAddEndpointTask(extension, configuration)
        configureCollectSchemesTask(extension, configuration)
    }

    private fun installValidations(configuration: ValidationConfiguration, extension: ContractsModuleExtension) {
        configureValidationTask(extension, configuration)
    }

    private fun registerCodegenVariantsTask(
        extension: ContractsModuleExtension
    ) {
        val target = project
        // KMP type is not supported for now, using only single target (android/jvm)
        val kotlinTargetExtension = target.kotlinExtension as? KotlinSingleTargetExtension<*> ?: return

        val rootExtension = project.rootProject.extensions.getByType<ContractsRootExtension>()
        val networkConfiguration = rootExtension.defaultNetwork

        val validationTask = project.tasks.withType<ValidateNetworkContractsTask>()

        kotlinTargetExtension.target.compilations
            .all { compilation ->
                // configure codegen task only for Android/Kotlin modules and include only baseVariant/main sourceSet
                val codegenTask = when {
                    compilation.isAndroidBaseVariantCompilation() -> registerCodegenTask(
                        kind = extension.kind,
                        projectName = extension.projectName,
                        variant = compilation.androidVariant.name,
                        target = target,
                        codegenConfiguration = extension.codegen,
                        schemesDirectory = extension.schemesBaseDirectory,
                        generators = extension.codegen.generators,
                        crtEnv = networkConfiguration.crtEnvName,
                        ketEnv = networkConfiguration.keyEnvName,
                    )

                    compilation.isJvmMainCompilation() -> registerCodegenTask(
                        kind = extension.kind,
                        projectName = extension.projectName,
                        target = target,
                        codegenConfiguration = extension.codegen,
                        schemesDirectory = extension.schemesBaseDirectory,
                        generators = extension.codegen.generators,
                        crtEnv = networkConfiguration.crtEnvName,
                        ketEnv = networkConfiguration.keyEnvName,
                    )

                    else -> return@all
                }

                compilation.defaultSourceSet.kotlin.srcDirs(codegenTask.flatMap { it.outputDirectory })
                codegenTask.configure {
                    it.mustRunAfter(validationTask)
                }
            }
    }

    private fun registerCodegenTask(
        kind: Provider<String>,
        projectName: Provider<String>,
        target: Project,
        variant: String = "",
        forceValidation: Boolean = false,
        codegenConfiguration: CodegenConfiguration,
        schemesDirectory: Provider<Directory>,
        generators: ListProperty<String>,
        crtEnv: Provider<String> = Providers.notDefined(),
        ketEnv: Provider<String> = Providers.notDefined(),
        action: (CodegenTask) -> Unit = {}
    ): TaskProvider<CodegenTask> {
        val setupMtlsTask = target.rootProject.tasks.named(
            SetupTmpMtlsFilesTask.NAME,
            SetupTmpMtlsFilesTask::class.java
        )

        val taskName = ContractsTaskNamesBuilder.codegenTask(variant)
        val outputDirectory = if (variant.isEmpty()) {
            codegenConfiguration.generatedDirectory.dir("main")
        } else {
            codegenConfiguration.generatedDirectory.dir(variant)
        }

        return target.tasks.register(taskName, CodegenTask::class.java) {
            it.packageName.set(codegenConfiguration.packageName)
            it.apiClassName.set(codegenConfiguration.apiClassName)
            it.moduleName.set(it.project.path)
            it.flags.set(codegenConfiguration.flags)
            it.mappings.set(codegenConfiguration.mappings)
            it.kind.set(kind)
            it.codegenProjectName.set(projectName)
            it.skipValidation.set(codegenConfiguration.skipValidation.map { !forceValidation && it })
            it.clearBeforeRun.set(codegenConfiguration.clearBeforeRun)
            it.moduleDirectory.set(it.project.layout.projectDirectory)
            it.outputDirectory.set(outputDirectory)
            it.generators.addAll(generators)
            it.codegenFile.set(it.project.file("codegen.toml"))
            it.errorOutputType.set(codegenConfiguration.errorOutputType)
            it.errorOutputTransformer.set(
                codegenConfiguration.errorOutputType
                    .flatMap { type -> codegenConfiguration.outputTransformers.named(type.kind) }
                    .flatMap { configuration -> configuration.transformer }
            )

            val codegenDependencyConfiguration = target.codegenDependencyConfiguration.takeIf { !it.isEmpty }
                ?: target.rootProject.codegenDependencyConfiguration
            it.codegenExecutableFiles.setFrom(codegenDependencyConfiguration.files)

            it.schemesDir.set(schemesDirectory)

            it.crtEnvName.set(crtEnv)
            it.keyEnvName.set(ketEnv)

            it.timeoutSeconds.set(codegenConfiguration.codegenTimeoutSeconds)

            if (forceValidation) {
                it.tmpCrtFile.set(setupMtlsTask.flatMap { it.tmpCrt })
                it.tmpKeyFile.set(setupMtlsTask.flatMap { it.tmpKey })
            }

            it.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(it))

            it.onlyIf { (it as? CodegenTask)?.schemesDir?.get()?.asFileTree?.isEmpty == false }
            action.invoke(it)
        }
    }

    private fun configureAddEndpointTask(
        extension: ContractsModuleExtension,
        variantConfiguration: ImportConfiguration,
    ) {
        project.tasks.register(
            ContractsTaskNamesBuilder.importSchemeTask(variantConfiguration.name),
            ApiSchemesImportTask::class.java
        ) {
            it.apiPath.set(project.getOptionalStringProperty("avito.networkContracts.schemesPath", ""))
            it.outputDirectory.set(
                extension.schemesBaseDirectory
                    .flatMap { it.dir(variantConfiguration.schemesDirName) }
            )

            it.importService.set(variantConfiguration.importService)

            it.loggerFactory.set(GradleLoggerPlugin.provideLoggerFactory(project))
            it.analyticsTrackerService.set(NetworkContractsAnalyticsService.provideService(project))
        }
    }

    private fun configureValidationTask(
        extension: ContractsModuleExtension,
        variantConfiguration: ValidationConfiguration,
    ) {

        val rootTask = project.rootProject.tasks.withType<CompositeTask>()
            .named(ContractsTaskNamesBuilder.validationTask("all"))

        val compositeTask = project.tasks.register<CompositeTask>(
            name = ContractsTaskNamesBuilder.validationTask(
                variantConfiguration.name,
                subVariant = "all"
            )
        )
        val updateApiSchemesTask = project.rootProject.tasks.typedNamedOrNull<UpdateRemoteApiSchemesTask>(
            ContractsTaskNamesBuilder.updateSchemesTask(variantConfiguration.name)
        )

        val dependentTasks = project.tasks.withType(ValidateNetworkContractsTask::class.java)

        variantConfiguration.rulesGroups.all { group ->
            val validationTask = project.registerValidationTask(
                ContractsTaskNamesBuilder.validationTask(
                    variantConfiguration.name,
                    subVariant = group.name
                )
            ) {
                rules.set(group.rules)
                requiredVerdicts.from(
                    group.dependsOn
                        .map {
                            it.map { dependentGroup ->
                                dependentTasks.named(
                                    ContractsTaskNamesBuilder.validationTask(
                                        variantConfiguration.name,
                                        subVariant = dependentGroup
                                    )
                                )
                                    .flatMap { it.verdictFile }
                            }
                        }
                )
                kind.set(extension.kind)
                variantName.set(variantConfiguration.name)
                onlyIf { group.onlyIf.get() }
            }
            compositeTask.configure { it.reports.from(validationTask.flatMap { it.verdictFile }) }
        }

        rootTask.configure {
            it.reports.from(compositeTask.map { it.output })
        }

        updateApiSchemesTask?.configure {
            it.validationReports.from(compositeTask.map { it.output })
        }
    }

    private fun configureCollectSchemesTask(
        extension: ContractsModuleExtension,
        configuration: ImportConfiguration
    ) {
        val updateApiSchemesTask = project.rootProject.tasks.typedNamedOrNull<UpdateRemoteApiSchemesTask>(
            ContractsTaskNamesBuilder.updateSchemesTask(configuration.name)
        )

        val collectApiSchemesTask = project.tasks.register<CollectApiSchemesTask>(
            ContractsTaskNamesBuilder.collectSchemesTask(configuration.name)
        ) {
            projectPath.set(project.path)
            kind.set(configuration.name)
            projectName.set(extension.projectName)
            codegenTomlFile.set(
                project.layout.projectDirectory
                    .asFileTree
                    .matching { it.include("**/codegen.toml") }
                    .firstOrNull()
            )
            schemesDirectory.set(extension.schemesBaseDirectory.flatMap { it.dir(configuration.schemesDirName) })

            jsonSchemeMetadataFile.set(
                project.reportFile(
                    directory = "networkContracts",
                    reportFileName = "${ContractsTaskNamesBuilder.collectSchemesTask(configuration.name)}.json"
                )
            )
        }

        updateApiSchemesTask?.configure {
            it.schemes.from(collectApiSchemesTask.flatMap(CollectApiSchemesTask::jsonSchemeMetadataFile))
        }
    }
}

private fun Project.registerValidationTask(
    name: String,
    builder: ValidateNetworkContractsTask.() -> Unit = {}
): TaskProvider<ValidateNetworkContractsTask> {
    val path = path
    return tasks.register<ValidateNetworkContractsTask>(name).apply {
        configure { task ->
            task.verdictFile.set(
                project.reportFile(
                    directory = "networkContracts",
                    reportFileName = "${name}Verdict.txt"
                )
            )

            task.modulePath.set(path)
            task.analyticsTrackerService.set(NetworkContractsAnalyticsService.provideService(project))
            builder.invoke(task)
        }
    }
}

@OptIn(ExperimentalContracts::class)
private fun KotlinCompilation<*>.isAndroidBaseVariantCompilation(): Boolean {
    contract {
        returns(true) implies (this@isAndroidBaseVariantCompilation is KotlinJvmAndroidCompilation)
    }
    return this is KotlinJvmAndroidCompilation &&
        this.androidVariant.baseName == this.androidVariant.name
}

@OptIn(ExperimentalContracts::class)
private fun KotlinCompilation<*>.isJvmMainCompilation(): Boolean {
    contract {
        returns(true) implies (this@isJvmMainCompilation is KotlinWithJavaCompilation<*, *>)
    }
    return this is KotlinWithJavaCompilation<*, *> && this.name == "main"
}
