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

    data class TestSuite(
        val appliedFilter: TestsFilter,
        val testsToRun: List<TestWithTarget>,
        val skippedTests: List<Pair<TestWithTarget, Excluded>>
    )

    fun getTestSuite(tests: List<TestInApk>): TestSuite

    class Impl(
        private val report: Report,
        private val targets: List<TargetConfiguration.Data>,
        private val reportSkippedTests: Boolean,
        private val filterFactory: FilterFactory
    ) : TestSuiteProvider {

        override fun getTestSuite(
            tests: List<TestInApk>
        ): TestSuite {

            val suite = getTestSuite(
                tests = tests,
                filter = filterFactory.createFilter()
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
