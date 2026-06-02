package com.avito.instrumentation.internal

import com.android.build.api.variant.Variant
import com.avito.android.plugins.configuration.BuildEnvResolver
import com.avito.android.plugins.configuration.GitResolver
import com.avito.android.plugins.configuration.RunIdResolver
import com.avito.git.gitInfoService
import com.avito.instrumentation.configuration.ExecutionEnvironment
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.instrumentation_args.AgpInstrumentationArgsProvider
import com.avito.instrumentation_args.InstrumentationArgsResolver
import com.avito.instrumentation_args.LocalRunInteractor
import com.avito.instrumentation_args.SetupLocalInstrumentationArgsUseCase
import com.avito.logger.GradleLoggerPlugin
import com.avito.reportviewer.model.RunId
import com.avito.utils.gradle.envArgs
import org.gradle.api.Project

internal class ConfiguratorsFactory(
    private val project: Project,
    private val extension: InstrumentationTestsPluginExtension,
    private val buildCacheEnabled: Boolean,
) {
    private val gitResolver = GitResolver(project.gitInfoService().map { it.getGitState() })

    // todo envArgs should be lazy, see [com.avito.kotlin.dsl.ProjectProperty]
    private val buildEnvResolver = BuildEnvResolver(project.provider { project.envArgs })

    private val runIdResolver = RunIdResolver(
        gitResolver = gitResolver,
        buildEnvResolver = buildEnvResolver
    )

    private val reportResolver = ReportResolver(extension, runIdResolver, project.providers)

    private val androidVariantConfiguratorFactory = AndroidVariantConfiguratorFactory(extension)

    private val outputDirResolver = OutputDirResolver(
        extension = extension,
    )

    private val argsTester = LocalRunArgsChecker(outputDirResolver)

    private val experimentsConfigurator = ExperimentsConfigurator(extension)

    private val loggerFactory = GradleLoggerPlugin.getLoggerFactory(project)

    private val agpInstrumentationArgsProvider = AgpInstrumentationArgsProvider()

    private val instrumentationArgsResolver = InstrumentationArgsResolver(
        agpArgsProvider = agpInstrumentationArgsProvider,
        extensionArgsProvider = ExtensionInstrumentationArgsProvider(extension),
        additionalArgsProviders = listOf(
            ReportInstrumentationArgsProvider(
                reportResolver = reportResolver,
                // The SendFromDevice report runId is injected into AGP's eager
                // testInstrumentationRunnerArguments DSL at configuration time, and is only ever
                // produced for local SendFromDevice runs (CI uses SendFromRunner). It is report
                // metadata, not a build input, so it must not pull git into the configuration
                // cache (which would invalidate CC on every branch switch / commit). Use a stable,
                // git-independent value. See MBSA-2359.
                runIdProvider = { RunId(identifier = "local", buildTypeId = project.envArgs.build.type) },
            ),
        ),
    )

    val setupLocalInstrumentationArgsUseCase = SetupLocalInstrumentationArgsUseCase(
        agpInstrumentationArgsProvider = agpInstrumentationArgsProvider,
        localRunInteractor = LocalRunInteractor(
            instrumentationArgsDumper = argsTester,
            instrumentationArgsResolver = instrumentationArgsResolver,
        ),
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

            val instrumentationConfigurator = InstrumentationConfigurator(
                extension = extension,
                configuration = configuration,
                instrumentationArgsResolver = instrumentationArgsResolver,
                reportResolver = reportResolver,
                providerFactory = project.providers,
                loggerFactory = loggerFactory,
            )

            val buildCacheConfigurator = BuildCacheConfigurator(
                buildCacheEnabled = buildCacheEnabled,
            )

            listOf(
                androidVariantConfigurator,
                changedTestsConfigurator,
                instrumentationConfigurator,
                experimentsConfigurator,
                outputDirConfigurator,
                buildCacheConfigurator,
                EnvironmentConfigurator(environment),
                GitConfigurator(gitResolver),
                CIArgsConfigurator(buildEnvResolver),
            )
        } else {
            null
        }
    }
}
