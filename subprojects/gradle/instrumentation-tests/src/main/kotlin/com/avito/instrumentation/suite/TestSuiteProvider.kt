package com.avito.instrumentation.suite

import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.filter.FilterFactory
import com.avito.instrumentation.suite.filter.TestsFilter
import com.avito.instrumentation.suite.filter.TestsFilter.Result.Excluded
import com.avito.instrumentation.suite.filter.TestsFilter.Signatures
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.report.model.DeviceName

/**
 * todo MBS-8045 сделать подмножество конфига для фильтров, сейчас тащим сюда огромный объект InstrumentationTestsAction.Params
 */
interface TestSuiteProvider {

    fun getInitialTestSuite(tests: List<TestInApk>): List<TestWithTarget>
    fun getRerunTestsSuite(tests: List<TestInApk>): List<TestWithTarget>

    class Impl(
        private val report: Report,
        private val targets: List<TargetConfiguration.Data>,
        private val reportSkippedTests: Boolean,
        private val filterFactory: FilterFactory
    ) : TestSuiteProvider {

        private data class TestSuite(
            val testsToRun: List<TestWithTarget>,
            val skippedTests: List<Pair<TestWithTarget, Excluded>>
        )

        override fun getInitialTestSuite(
            tests: List<TestInApk>
        ): List<TestWithTarget> {

            val suite = getTestSuite(
                tests = tests,
                filter = filterFactory.createInitialFilter()
            )

            if (reportSkippedTests) {
                report.sendSkippedTests(
                    skippedTests = suite.skippedTests
                        /**
                         * Не репортим скипы по причине "тест уже прошел на этом коммите" т.к репорт вьювер финальным статусом теста
                         * считает его последний статус. Так, в итоге, в репорт вьювере у нас отображаются все прошедшие тесты как
                         * заскипанные.
                         */
                        .filter { (_, verdict) ->
                            (verdict !is Excluded.BySignatures) ||
                                    (verdict.source != Signatures.Source.PreviousRun)
                        }
                        .map { (targetTest, verdict) ->
                            targetTest.test to verdict.reason
                        }
                )
            }

            return suite.testsToRun
        }

        override fun getRerunTestsSuite(
            tests: List<TestInApk>
        ): List<TestWithTarget> {
            return getTestSuite(
                tests = tests,
                filter = filterFactory.createRerunFilter()
            ).testsToRun
        }

        private fun getTestSuite(
            tests: List<TestInApk>,
            filter: TestsFilter
        ): TestSuite {
            val source = targets.flatMap { target ->
                val deviceName = DeviceName(target.deviceName)
                tests.map { testInApk ->
                    TestWithTarget(
                        test = parseTest(testInApk, deviceName),
                        target = target
                    ) to filter.filter(
                        TestsFilter.Test(
                            name = testInApk.testName.name,
                            annotations = testInApk.annotations,
                            deviceName = deviceName,
                            api = target.reservation.device.api
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
                testsToRun = testsToRun,
                skippedTests = skippedTests
            )
        }
    }
}
