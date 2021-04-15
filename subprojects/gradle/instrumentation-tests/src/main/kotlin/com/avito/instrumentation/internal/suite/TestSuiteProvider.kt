package com.avito.instrumentation.internal.suite

import com.avito.android.TestInApk
import com.avito.android.runner.report.LegacyReport
import com.avito.android.runner.report.Report
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.TestsFilter
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Result.Excluded
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Signatures
import com.avito.instrumentation.internal.suite.model.TestWithTarget
import com.avito.instrumentation.suite.parseTest
import com.avito.report.model.AndroidTest
import com.avito.report.model.DeviceName
import com.avito.time.TimeProvider

internal interface TestSuiteProvider {

    data class TestSuite(
        val appliedFilter: TestsFilter,
        val testsToRun: List<TestWithTarget>,
        val skippedTests: List<Pair<TestWithTarget, Excluded>>
    )

    fun getTestSuite(tests: List<TestInApk>): TestSuite

    class Impl(
        private val report: Report,
        private val legacyReport: LegacyReport,
        private val targets: List<TargetConfiguration.Data>,
        private val reportSkippedTests: Boolean,
        private val filterFactory: FilterFactory,
        private val timeProvider: TimeProvider,
        private val useInMemoryReport: Boolean
    ) : TestSuiteProvider {

        override fun getTestSuite(tests: List<TestInApk>): TestSuite {

            val suite = getTestSuite(
                tests = tests,
                filter = filterFactory.createFilter()
            )

            if (reportSkippedTests) {
                val skippedTests = suite.skippedTests
                    // do not report skip here, to prevent final test status rewrite (green from last run - ok)
                    .filter { (_, verdict) ->
                        verdict !is Excluded.BySignatures || verdict.source != Signatures.Source.PreviousRun
                    }
                    .map { (targetTest, verdict) ->
                        targetTest.test to verdict.reason
                    }

                if (useInMemoryReport) {
                    skippedTests.forEach { (testStaticData, reason) ->
                        report.addTest(
                            AndroidTest.Skipped.fromTestMetadata(
                                testStaticData = testStaticData,
                                skipReason = reason,
                                reportTime = timeProvider.nowInSeconds()
                            )
                        )
                    }
                } else {
                    legacyReport.sendSkippedTests(skippedTests)
                }
            }

            return suite
        }

        private fun getTestSuite(
            tests: List<TestInApk>,
            filter: TestsFilter
        ): TestSuite {
            val source = targets.flatMap { target ->
                val deviceName = DeviceName(target.deviceName)
                tests.map { testInApk ->
                    val testStaticData = parseTest(testInApk, deviceName, target.reservation.device.api)
                    TestWithTarget(
                        test = testStaticData,
                        target = target
                    ) to filter.filter(
                        TestsFilter.Test(
                            name = testInApk.testName.name,
                            annotations = testInApk.annotations,
                            deviceName = deviceName,
                            api = target.reservation.device.api,
                            flakiness = testStaticData.flakiness
                        )
                    )
                }
            }

            val skippedTests =
                source.filter { (_, verdict) -> verdict !is TestsFilter.Result.Included }
                    .map { (test, verdict) -> test to verdict as Excluded }

            val testsToRun =
                source.filter { (_, verdict) -> verdict is TestsFilter.Result.Included }
                    .map { (test, _) -> test }

            return TestSuite(
                appliedFilter = filter,
                testsToRun = testsToRun,
                skippedTests = skippedTests
            )
        }
    }
}
