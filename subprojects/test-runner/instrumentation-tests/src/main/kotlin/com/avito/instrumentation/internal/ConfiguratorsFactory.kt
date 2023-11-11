package com.avito.instrumentation.internal

import com.android.build.api.variant.Variant
import com.avito.android.plugins.configuration.BuildEnvResolver
import com.avito.android.plugins.configuration.GitResolver
import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.git.gitState
import com.avito.instrumentation.configuration.ExecutionEnvironment
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.utils.gradle.envArgs
import org.gradle.api.Project

internal class ConfiguratorsFactory(
    private val project: Project,
    private val extension: InstrumentationTestsPluginExtension,
) {
    private val gitResolver = GitResolver(project.gitState())

    // todo envArgs should be lazy, see [com.avito.kotlin.dsl.ProjectProperty]
    private val buildEnvResolver = BuildEnvResolver(project.provider { project.envArgs })

    private val runIdResolver = RunIdResolver(
        gitResolver = gitResolver,
        buildEnvResolver = buildEnvResolver
    )

    private val reportResolver = ReportResolver(extension, runIdResolver)

    private val androidVariantConfiguratorFactory = AndroidVariantConfiguratorFactory(extension)

    private val outputDirResolver = OutputDirResolver(
        extension = extension,
    )

    private val argsTester = LocalRunArgsChecker(outputDirResolver)

    private val androidDslInteractor = AndroidDslInteractor

    private val experimentsConfigurator = ExperimentsConfigurator(extension)

    val instrumentationArgsResolver = InstrumentationArgsResolver(
        extension = extension,
        reportResolver = reportResolver,
        runIdResolver = runIdResolver,
        androidDslInteractor = androidDslInteractor,
    )

    val localRunInteractor: LocalRunInteractor = LocalRunInteractor(
        argsTester = argsTester,
        dslInteractor = androidDslInteractor,
        logger = project.logger
    )

    fun createTaskConfigurators(
        configuration: InstrumentationConfiguration,
        environment: ExecutionEnvironment,
        variant: Variant
    ): List<InstrumentationTaskConfigurator>? {

        val androidVariantConfigurator = androidVariantConfiguratorFactory.createConfigurator(variant)

        return if (androidVariantConfigurator != null) {

            val changedTestsConfigurator = ChangedTestsConfigurator(
                pluginContainer = project.plugins,
                taskContainer = project.tasks,
                configuration = configuration,
            )

            val outputDirConfigurator = OutputDirConfigurator(
                extension = extension,
                reportResolver = reportResolver,
                outputDirResolver = outputDirResolver,
                configuration = configuration
            )

            val outputDir = outputDirConfigurator.resolve(configuration)

            val instrumentationConfigurator = InstrumentationConfigurator(
                extension = extension,
                configuration = configuration,
                instrumentationArgsResolver = instrumentationArgsResolver,
                outputDir = outputDir,
                reportResolver = reportResolver,
            )

            listOf(
                androidVariantConfigurator,
                changedTestsConfigurator,
                instrumentationConfigurator,
                experimentsConfigurator,
                outputDirConfigurator,
                EnvironmentConfigurator(environment),
                GitConfigurator(gitResolver),
                CIArgsConfigurator(buildEnvResolver),
            )
        } else {
            null
        }
    }
}
