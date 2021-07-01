package com.avito.runner.scheduler.suite

import com.avito.android.TestInApk
import com.avito.android.runner.report.Report
import com.avito.runner.config.TargetConfigurationData
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.TestsFilter
import com.avito.runner.scheduler.suite.filter.TestsFilter.Result.Excluded

internal interface TestSuiteProvider {

    fun getTestSuite(tests: List<TestInApk>): TestSuite

    class Impl(
        private val report: Report,
        private val targets: List<TargetConfigurationData>,
        private val reportSkippedTests: Boolean,
        private val filterFactory: FilterFactory
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
                        verdict !is Excluded.BySignatures || verdict.source != TestsFilter.Signatures.Source.PreviousRun
                    }
                    .map { (test, verdict) ->
                        test to verdict.reason
                    }

                report.addSkippedTests(skippedTests)
            }

            return suite
        }

        private fun getTestSuite(
            tests: List<TestInApk>,
            filter: TestsFilter
        ): TestSuite {
            val source = targets.flatMap { target ->
                val deviceName = target.deviceName
                tests.map { testInApk ->
                    val testStaticData = parseTest(testInApk, deviceName, target.reservation.device.api)
                    testStaticData to filter.filter(
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
