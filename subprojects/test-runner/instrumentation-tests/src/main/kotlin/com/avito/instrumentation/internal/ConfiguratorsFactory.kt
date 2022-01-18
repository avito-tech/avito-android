package com.avito.instrumentation.internal

import com.android.build.api.variant.Variant
import com.avito.git.gitState
import com.avito.instrumentation.configuration.ExecutionEnvironment
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.time.DefaultTimeProvider
import com.avito.time.TimeProvider
import com.avito.utils.gradle.envArgs
import org.gradle.api.Project

internal class ConfiguratorsFactory(
    private val project: Project,
    private val extension: InstrumentationTestsPluginExtension,
) {
    private val timeProvider: TimeProvider = DefaultTimeProvider()

    private val gitResolver = GitResolver(project.gitState())

    // todo envArgs should be lazy, see [com.avito.kotlin.dsl.ProjectProperty]
    private val buildEnvResolver = BuildEnvResolver(project.provider { project.envArgs })

    private val runIdResolver = RunIdResolver(
        timeProvider = timeProvider,
        gitResolver = gitResolver,
        buildEnvResolver = buildEnvResolver
    )

    private val reportResolver = ReportResolver(extension, runIdResolver)

    private val androidVariantConfiguratorFactory = AndroidVariantConfiguratorFactory()

    private val planSlugResolver = PlanSlugResolver

    private val sentryResolver = SentryResolver(extension, project.providers)

    private val outputDirResolver = OutputDirResolver(
        extension = extension,
        rootProjectLayout = project.rootProject.layout,
        providers = project.providers,
        logger = project.logger,
    )

    private val argsTester = LocalRunArgsChecker(outputDirResolver)

    private val androidDslInteractor = AndroidDslInteractor

    private val experimentsConfigurator = ExperimentsConfigurator(project, extension)

    val instrumentationArgsResolver = InstrumentationArgsResolver(
        extension = extension,
        sentryResolver = sentryResolver,
        reportResolver = reportResolver,
        planSlugResolver = planSlugResolver,
        runIdResolver = runIdResolver,
        buildEnvResolver = buildEnvResolver,
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
                reportResolver = reportResolver,
                outputDirResolver = outputDirResolver,
                configuration = configuration
            )

            val outputDir = outputDirConfigurator.resolve(configuration)

            val instrumentationConfigurator = InstrumentationConfigurator(
                extension = extension,
                configuration = configuration,
                instrumentationArgsResolver = instrumentationArgsResolver,
                outputDir = outputDir
            )

            listOf(
                androidVariantConfigurator,
                changedTestsConfigurator,
                instrumentationConfigurator,
                experimentsConfigurator,
                outputDirConfigurator,
                EnvironmentConfigurator(environment),
                GitConfigurator(gitResolver),
                ReportViewerConfigurator(reportResolver),
                CIArgsConfigurator(buildEnvResolver),
            )
        } else {
            null
        }
    }
}
