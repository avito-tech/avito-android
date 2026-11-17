package com.avito.runner.scheduler.suite

import com.avito.android.TestInApk
import com.avito.report.ReportViewerTestStaticDataParser
import com.avito.report.model.TestStaticData
import com.avito.runner.scheduler.suite.filter.FilterFactory
import com.avito.runner.scheduler.suite.filter.TestsFilter
import com.avito.runner.scheduler.suite.filter.TestsFilter.Verdict

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
            val testsToRun = mutableListOf<TestStaticData>()
            val skippedTests = mutableListOf<Pair<TestStaticData, Verdict.Skipped>>()

            testStaticParser.getTestSuite(tests).forEach { rawParsedTest ->
                val testStaticData = rawParsedTest.testStaticData
                val target = rawParsedTest.target
                val verdict = filter.filter(
                    TestsFilter.Test(
                        name = testStaticData.name.name,
                        annotations = rawParsedTest.annotations,
                        deviceName = target.name,
                        api = target.api,
                        flakiness = testStaticData.flakiness
                    )
                )
                when (verdict) {
                    Verdict.Included -> testsToRun += testStaticData
                    is Verdict.Skipped -> skippedTests += testStaticData to verdict
                    is Verdict.NotForTarget -> Unit
                }
            }

            return TestSuite(
                appliedFilter = filter,
                testsToRun = testsToRun,
                skippedTests = skippedTests
            )
        }
    }
}
