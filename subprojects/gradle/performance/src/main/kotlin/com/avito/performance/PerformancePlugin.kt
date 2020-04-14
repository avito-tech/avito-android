package com.avito.performance

import com.avito.git.gitState
import com.avito.git.isOnDefaultBranch
import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.instrumentationTask
import com.avito.instrumentation.withInstrumentationTests
import com.avito.kotlin.dsl.fileProperty
import com.avito.kotlin.dsl.toOptional
import com.avito.utils.gradle.envArgs
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.provider.Providers
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

@Suppress("UnstableApiUsage")
open class PerformancePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create<PerformanceExtension>("performance")

        val gitState = project.gitState { project.ciLogger.info(it) }

        val envArgs = project.envArgs

        @Suppress("UnstableApiUsage")
        val graphiteKeyProvider = gitState.map {
            if (it.isOnDefaultBranch) {
                it.defaultBranch
            } else {
                "not-defined"
            }
        }.orElse("not-defined")

        project.withInstrumentationTests { instrumentationConfig ->

            instrumentationConfig.configurations
                .filter { it.performanceType == InstrumentationConfiguration.PerformanceType.MDE }
                .forEach { performanceMdeConfig ->

                    val performanceResultsFile = File(
                        extension.output,
                        "${performanceMdeConfig.name}_${extension.performanceTestResultName}"
                    )

                    project.tasks.instrumentationTask(performanceMdeConfig.name) { instrumentationTask ->

                        val performanceMdeCollectProvider =
                            project.tasks.register<PerformanceCollectTask>("collect${performanceMdeConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Collect performance data nightly"
                                graphiteKey.set(graphiteKeyProvider)
                                performanceTests.set(performanceResultsFile)
                                buildId.set(envArgs.build.id.toString())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                reportCoordinates.set(performanceMdeConfig.instrumentationParams.reportCoordinates())
                                dependsOn(instrumentationTask)
                            }

                        project.tasks.register<SendPerformanceMdeTask>("sendPerformanceMdeTask") {
                            group = TASK_GROUP
                            description = "Report for mde calculations"
                            currentTests.set(performanceResultsFile)
                            statsUrl.set(extension.statsUrl)
                            dependsOn(performanceMdeCollectProvider)
                        }
                    }
                }

            instrumentationConfig.configurations
                .filter { it.performanceType == InstrumentationConfiguration.PerformanceType.SIMPLE }
                .forEach { performanceConfig ->

                    val performanceResultsFile = File(
                        extension.output,
                        "${performanceConfig.name}_${extension.performanceTestResultName}"
                    )

                    val reportCoordinates = performanceConfig.instrumentationParams.reportCoordinates()

                    project.tasks.instrumentationTask(performanceConfig.name) { instrumentationTask ->

                        val performanceCollectProvider =
                            project.tasks.register<PerformanceCollectTask>("collect${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Collect performance data"
                                graphiteKey.set(graphiteKeyProvider)
                                performanceTests.set(performanceResultsFile)
                                this.reportCoordinates.set(reportCoordinates)
                                buildId.set(envArgs.build.id.toString())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                dependsOn(instrumentationTask)
                            }

                        val previousPerformanceTestResultName =
                            "previous_${performanceConfig.name}_${extension.performanceTestResultName}"

                        val targetBranch =
                            gitState.map {
                                // падаем раньше с более понятной ошибкой, иначе упадем т.к. graphiteKey не @Optional
                                requireNotNull(it.targetBranch?.name)
                                { "Can't run performance tasks without targetBranch specified" }
                            }

                        val performanceDownloadProvider =
                            project.tasks.register<PerformanceCollectTask>("download${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Download performance report from last build"
                                graphiteKey.set(targetBranch)
                                performanceTests.set(
                                    File(
                                        extension.output,
                                        previousPerformanceTestResultName
                                    )
                                )
                                this.reportCoordinates.set(reportCoordinates.copy(jobSlug = "${reportCoordinates.jobSlug}-${InstrumentationTestsAction.RUN_ON_TARGET_BRANCH_SLUG}"))
                                buildId.set(envArgs.build.id.toString())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                dependsOn(performanceCollectProvider)
                            }

                        val previousTestsFile = project.fileProperty(
                            File(
                                extension.output,
                                previousPerformanceTestResultName
                            )
                        )

                        val performanceCompareProvider =
                            project.tasks.register<PerformanceCompareTask>("compare${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Compare performance reports"
                                comparison.set(File(extension.output, "comparison.json"))
                                currentTests.set(performanceResultsFile)
                                previousTests.set(previousTestsFile.toOptional())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                statsUrl.set(extension.statsUrl)
                                dependsOn(performanceCollectProvider)
                                dependsOn(performanceDownloadProvider)
                            }

                        val performanceProvider = gitState.map {
                            when {
                                it.isOnDefaultBranch -> performanceCollectProvider
                                it.targetBranch != null -> performanceCompareProvider
                                else -> Providers.notDefined<Task>()
                            }
                        }

                        project.tasks.register<Task>(measurePerformanceTaskName(performanceConfig.name)) {
                            this.group = TASK_GROUP
                            this.description = "Measure performance"

                            if (performanceProvider.isPresent) {
                                this.dependsOn(performanceProvider)
                            }
                            this.onlyIf { performanceProvider.isPresent }
                        }
                    }
                }
        }
    }
}

private const val TASK_GROUP = "performance"
