package com.avito.runner.scheduler.suite

import com.avito.android.TestInApk
import com.avito.report.Report
import com.avito.report.ReportViewerTestStaticDataParser
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.TestsFilter
import com.avito.runner.scheduler.suite.filter.TestsFilter.Result.Excluded

internal interface TestSuiteProvider {

    fun getTestSuite(tests: List<TestInApk>): TestSuite

    class Impl(
        private val report: Report,
        private val reportSkippedTests: Boolean,
        private val filterFactory: FilterFactory,
        private val testStaticParser: ReportViewerTestStaticDataParser,
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
            val source = testStaticParser.getTestSuite(tests).map { rawParsedTest ->
                val testStaticData = rawParsedTest.testStaticData
                val target = rawParsedTest.target
                testStaticData to filter.filter(
                    TestsFilter.Test(
                        name = testStaticData.name.name,
                        annotations = rawParsedTest.annotations,
                        deviceName = target.name,
                        api = target.api,
                        flakiness = testStaticData.flakiness
                    )
                )
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
