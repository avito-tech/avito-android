package com.avito.runner.scheduler.suite

import com.avito.android.TestInApk
import com.avito.report.ReportViewerTestStaticDataParser
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.TestsFilter
import com.avito.runner.scheduler.suite.filter.TestsFilter.Result.Excluded

public interface TestSuiteProvider {

    public fun getTestSuite(tests: List<TestInApk>): TestSuite

    public class Impl(
        private val filterFactory: FilterFactory,
        private val testStaticParser: ReportViewerTestStaticDataParser,
    ) : TestSuiteProvider {

        override fun getTestSuite(tests: List<TestInApk>): TestSuite {

            val suite = getTestSuite(
                tests = tests,
                filter = filterFactory.createFilter()
            )

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
