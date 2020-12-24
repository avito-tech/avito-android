package com.avito.instrumentation.impact

import com.avito.android.percentOf
import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.graphiteSeries
import com.avito.android.stats.statsd
import com.avito.bytecode.report.JsonFileReporter
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.Test
import com.avito.logger.GradleLoggerFactory
import com.avito.utils.rewriteNewLineList
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class AnalyzeTestImpactTask @Inject constructor(
    config: InstrumentationTestImpactAnalysisExtension,
    private val finder: ModifiedProjectsFinder
) : DefaultTask() {

    @InputFile
    val bytecodeAnalyzeSummaryJson: RegularFileProperty = project.objects.fileProperty()

    @Optional
    @Input
    val packageFilter: Property<String> = config.packageFilter

    @Internal
    val impactSummaryFile: Provider<RegularFile> = config.output.file("impact-summary.json")

    /**
     * test code has been added
     */
    @OutputFile
    val addedTestsFile: Provider<RegularFile> = config.output.file("tests-added.txt")

    @OutputFile
    val modifiedTestsFile: Provider<RegularFile> = config.output.file("tests-modified.txt")

    /**
     * combination of changed and impacted
     */
    @OutputFile
    val testsToRunFile: Provider<RegularFile> = config.output.file("tests-to-run.txt")

    private val environmentInfo = project.environmentInfo().get()
    private val statsd = project.statsd.get()

    @Suppress("unused")
    @TaskAction
    fun findAffectedTasks() {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val loggerFactory = GradleLoggerFactory.fromTask(this)

        val impactSummary = AnalyzeTestImpactAction(
            bytecodeAnalyzeSummary = gson.fromJson(bytecodeAnalyzeSummaryJson.get().asFile.reader()),
            targetModule = AndroidProject(project),
            packageFilter = packageFilter.orNull,
            finder = finder,
            loggerFactory = loggerFactory
        ).computeImpact()

        JsonFileReporter(
            path = impactSummaryFile.get().asFile,
            gson = gson
        ).report(impactSummary)

        reportImpactAnalysisEfficiency(
            allTests = impactSummary.allTests,
            testsToRun = impactSummary.testsToRun
        )
        addedTestsFile.get().asFile.rewriteNewLineList(impactSummary.addedTests)
        modifiedTestsFile.get().asFile.rewriteNewLineList(impactSummary.modifiedTests)
        testsToRunFile.get().asFile.rewriteNewLineList(impactSummary.testsToRun)
    }

    private fun reportImpactAnalysisEfficiency(
        allTests: Set<Test>,
        testsToRun: Set<Test>
    ) {
        val envName = environmentInfo.environment.publicName
        val node = environmentInfo.node?.take(GRAPHITE_SERIES_LIMIT) ?: "_"
        val prefix = graphiteSeries(envName, node)

        // efficiency exists only if we have at least one test
        if (allTests.isNotEmpty()) {
            val percentage = testsToRun.size.percentOf(allTests.size)

            statsd.send(prefix, GaugeMetric("tia.ui_tests_ratio", percentage.toInt()))
        }
    }
}

/**
 * todo check if this is a real reason
 */
private const val GRAPHITE_SERIES_LIMIT = 32
