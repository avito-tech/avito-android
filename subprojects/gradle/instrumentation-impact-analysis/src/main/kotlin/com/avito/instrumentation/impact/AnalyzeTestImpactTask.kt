package com.avito.instrumentation.impact

import com.avito.android.sentry.environmentInfo
import com.avito.android.stats.GaugeMetric
import com.avito.android.stats.graphiteSeries
import com.avito.android.stats.statsd
import com.avito.bytecode.report.JsonFileReporter
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.Test
import com.avito.utils.logging.ciLogger
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
import kotlin.math.roundToInt

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

        val impactSummary = AnalyzeTestImpactAction(
            bytecodeAnalyzeSummary = gson.fromJson(bytecodeAnalyzeSummaryJson.get().asFile.reader()),
            targetModule = AndroidProject(project),
            packageFilter = packageFilter.orNull,
            finder = finder,
            ciLogger = ciLogger
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
        testsToRunFile.get().asFile.rewriteNewLineList(impactSummary.testsToRun)
    }

    private fun reportImpactAnalysisEfficiency(
        allTests: Set<Test>,
        testsToRun: Set<Test>
    ) {
        val envName = environmentInfo.environment.publicName
        val node = environmentInfo.node?.take(32) ?: "_"
        val prefix = graphiteSeries(envName, node)

        val percentage = ((testsToRun.size.toFloat() / allTests.size) * 100).roundToInt()

        statsd.send(prefix, GaugeMetric("tia.ui_tests_ratio", percentage))
    }
}
