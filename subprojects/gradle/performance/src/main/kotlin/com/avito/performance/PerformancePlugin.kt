package com.avito.performance

import com.avito.git.gitState
import com.avito.git.isOnDefaultBranch
import com.avito.instrumentation.InstrumentationTestsAction.Companion.RUN_ON_TARGET_BRANCH_SLUG
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.instrumentationTask
import com.avito.instrumentation.withInstrumentationTests
import com.avito.kotlin.dsl.dependencyOn
import com.avito.kotlin.dsl.toOptional
import com.avito.performance.PerformanceExtension.TargetBranchResultSource.FETCH_FROM_OTHER_BUILD
import com.avito.performance.PerformanceExtension.TargetBranchResultSource.RUN_IN_PROCESS
import com.avito.report.model.RunId
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

        val logger = project.ciLogger

        val gitState = project.gitState { logger.info(it) }

        val envArgs = project.envArgs

        val graphiteKeyProvider = gitState.map {
            if (it.isOnDefaultBranch) {
                it.defaultBranch
            } else {
                "not-defined"
            }
        }.orElse("not-defined")

        project.withInstrumentationTests { instrumentationConfig ->

            val performanceOutputDir = extension.output.get()
            val performanceResultsFileName = extension.performanceTestResultName.get()

            instrumentationConfig.configurations
                .filter { it.performanceType == InstrumentationConfiguration.PerformanceType.MDE }
                .forEach { performanceMdeConfig ->

                    logger.debug("Creating performance mde configuration: $performanceMdeConfig")

                    project.tasks.instrumentationTask(performanceMdeConfig.name) { instrumentationTask ->

                        val performanceMdeCollectProvider =
                            project.tasks.register<PerformanceCollectTask>("collect${performanceMdeConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Collect performance data nightly"
                                graphiteKey.set(graphiteKeyProvider)
                                performanceTests.set(
                                    File(
                                        performanceOutputDir,
                                        "${performanceMdeConfig.name}_${performanceResultsFileName}"
                                    )
                                )
                                buildId.set(envArgs.build.id.toString())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                reportCoordinates.set(performanceMdeConfig.instrumentationParams.reportCoordinates())
                                dependsOn(instrumentationTask)
                            }

                        project.tasks.register<SendPerformanceMdeTask>("sendPerformanceMdeTask") {
                            group = TASK_GROUP
                            description = "Report for mde calculations"
                            statsUrl.set(extension.statsUrl)

                            dependencyOn(performanceMdeCollectProvider) { mdeCollectTask ->
                                currentTests.set(mdeCollectTask.performanceTests)
                            }
                        }
                    }
                }

            instrumentationConfig.configurations
                .filter { it.performanceType == InstrumentationConfiguration.PerformanceType.SIMPLE }
                .forEach { performanceConfig ->

                    logger.debug("Creating performance configuration: $performanceConfig")

                    val reportCoordinates = performanceConfig.instrumentationParams.reportCoordinates()

                    project.tasks.instrumentationTask(performanceConfig.name) { instrumentationTask ->

                        logger.debug("Based on instrumentation task: $instrumentationTask")

                        val sourceBranchResultsCollector =
                            project.tasks.register<PerformanceCollectTask>("collect${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Collect performance data"
                                graphiteKey.set(graphiteKeyProvider)
                                performanceTests.set(
                                    File(
                                        performanceOutputDir,
                                        "${performanceConfig.name}_${performanceResultsFileName}"
                                    )
                                )
                                this.reportCoordinates.set(reportCoordinates)
                                buildId.set(envArgs.build.id.toString())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                dependsOn(instrumentationTask)
                            }

                        val targetBranchResultsCollector =
                            project.tasks.register<PerformanceCollectTask>("download${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Download performance report from target branch build"
                                graphiteKey.set(
                                    gitState.map {
                                        // fail fast for more meaningful error message
                                        requireNotNull(it.targetBranch?.name)
                                        { "Can't run performance tasks without targetBranch specified" }
                                    })
                                performanceTests.set(
                                    File(
                                        performanceOutputDir,
                                        "previous_${performanceConfig.name}_${performanceResultsFileName}"
                                    )
                                )
                                buildId.set(envArgs.build.id.toString())
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)

                                @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
                                when (extension.targetBranchResultSource.get()) {
                                    RUN_IN_PROCESS -> {
                                        //run on target branch is a part of the same task in `instrumentation plugin`
                                        dependsOn(sourceBranchResultsCollector)

                                        buildId.set(envArgs.build.id.toString())

                                        this.reportCoordinates.set(
                                            reportCoordinates.copy(
                                                jobSlug = "${reportCoordinates.jobSlug}-$RUN_ON_TARGET_BRANCH_SLUG"
                                            )
                                        )
                                    }
                                    FETCH_FROM_OTHER_BUILD -> {

                                        //there is no need to depend on tests, it is a hack to postpone fetching
                                        dependsOn(sourceBranchResultsCollector)

                                        //todo probably it's a mistake; but we need to fetch teamcity API otherwise
                                        buildId.set(null as String?)

                                        this.reportCoordinates.set(
                                            reportCoordinates.copy(
                                                runId = RunId(
                                                    commitHash = gitState.map { it.targetBranch!!.commit }.get(),
                                                    buildTypeId = extension.targetBuildConfigId.get()
                                                ).toString()
                                            )
                                        )
                                    }
                                }
                            }

                        val performanceCompareProvider =
                            project.tasks.register<PerformanceCompareTask>("compare${performanceConfig.name.capitalize()}") {
                                group = TASK_GROUP
                                description = "Compare performance reports"
                                comparison.set(File(performanceOutputDir, "comparison.json"))
                                reportApiUrl.set(instrumentationConfig.reportApiUrl)
                                reportApiFallbackUrl.set(instrumentationConfig.reportApiFallbackUrl)
                                statsUrl.set(extension.statsUrl)

                                dependencyOn(sourceBranchResultsCollector) { sourceTestsCollector ->
                                    sourceResults.set(sourceTestsCollector.performanceTests)
                                }

                                dependencyOn(targetBranchResultsCollector) { targetTestsCollector ->
                                    targetResults.set(targetTestsCollector.performanceTests.toOptional())
                                }
                            }

                        val performanceProvider = gitState.map {
                            when {
                                it.isOnDefaultBranch -> sourceBranchResultsCollector
                                it.targetBranch != null -> performanceCompareProvider
                                else -> Providers.notDefined<Task>()
                            }
                        }

                        logger.debug("performanceProvider=${performanceProvider.orNull}")

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
