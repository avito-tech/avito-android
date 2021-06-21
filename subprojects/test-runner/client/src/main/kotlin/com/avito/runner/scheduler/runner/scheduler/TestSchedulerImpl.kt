package com.avito.runner.scheduler.runner.scheduler

import com.avito.android.Result
import com.avito.android.TestInApk
import com.avito.android.TestSuiteLoader
import com.avito.android.check.AllChecks
import com.avito.android.runner.report.Report
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.finalizer.Finalizer
import com.avito.runner.scheduler.TestRunnerFactory
import com.avito.runner.scheduler.runner.model.TestRunnerResults
import com.avito.runner.scheduler.suite.TestSuite
import com.avito.runner.scheduler.suite.TestSuiteProvider
import com.avito.runner.scheduler.suite.filter.FilterInfoWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import java.io.File

internal class TestSchedulerImpl(
    private val report: Report,
    private val testSuiteProvider: TestSuiteProvider,
    private val testSuiteLoader: TestSuiteLoader,
    private val filterInfoWriter: FilterInfoWriter,
    private val testRunnerFactory: TestRunnerFactory,
    private val finalizer: Finalizer,
    private val filter: InstrumentationFilterData,
    private val testApk: File,
    private val outputDir: File,
    loggerFactory: LoggerFactory,
) : TestScheduler {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val logger = loggerFactory.create<TestSchedulerImpl>()

    override fun schedule(): TestSchedulerResult {
        logger.debug("Filter config: $filter")
        filterInfoWriter.writeFilterConfig(filter)

        val tests = testSuiteLoader.loadTestSuite(testApk, AllChecks())

        tests.fold(
            { result ->
                logger.info("Tests parsed from apk: ${result.size}")
                logger.debug("Tests parsed from apk: ${result.map { it.testName }}")
            },
            { error -> logger.critical("Can't parse tests from apk", error) }
        )

        writeParsedTests(outputDir, tests)

        val testSuite = testSuiteProvider.getTestSuite(
            tests = tests.getOrThrow()
        )

        val skippedTests = testSuite.skippedTests.map {
            "${it.first.test.name} on ${it.first.target.deviceName} because ${it.second.reason}"
        }
        logger.debug("Skipped tests: $skippedTests")

        val testsToRun = testSuite.testsToRun
        logger.debug("Tests to run: ${testsToRun.map { "${it.test.name} on ${it.target.deviceName}" }}")

        filterInfoWriter.writeAppliedFilter(testSuite.appliedFilter)
        filterInfoWriter.writeFilterExcludes(testSuite.skippedTests)

        writeTestSuite(outputDir, testSuite)

        if (testsToRun.isNotEmpty()) {
            runBlocking {
                testRunnerFactory.createTestRunner().runTests(testsToRun)
            }
        }

        val testSchedulerResult = TestRunnerResults(
            testsToRun = testSuite.testsToRun.map { it.test },
            testResults = report.getTestResults()
        )

        return finalizer.finalize(testSchedulerResult)
    }

    private fun writeParsedTests(outputDir: File, parsedTests: Result<List<TestInApk>>) {
        val file = File(outputDir, "parsed-tests.json")
        parsedTests.fold(
            { tests -> file.writeText(gson.toJson(tests)) },
            { t -> file.writeText("There was an error while parsing tests:\n $t") }
        )
    }

    private fun writeTestSuite(outputDir: File, testSuite: TestSuite) {
        File(outputDir, "test-suite.json")
            .writeText(gson.toJson(testSuite.testsToRun.map { it.test }))
    }
}
