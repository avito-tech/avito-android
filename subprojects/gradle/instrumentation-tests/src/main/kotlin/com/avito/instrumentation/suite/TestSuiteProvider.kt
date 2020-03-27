package com.avito.instrumentation.suite

import com.avito.instrumentation.InstrumentationTestsAction
import com.avito.instrumentation.configuration.target.TargetConfiguration
import com.avito.instrumentation.report.Report
import com.avito.instrumentation.suite.dex.TestInApk
import com.avito.instrumentation.suite.dex.TestSuiteLoader
import com.avito.instrumentation.suite.dex.TestSuiteLoaderImpl
import com.avito.instrumentation.suite.dex.check.TestSignatureCheck
import com.avito.instrumentation.suite.filter.AnnotatedWithFilter
import com.avito.instrumentation.suite.filter.CompositeTestRunFilter
import com.avito.instrumentation.suite.filter.DownsamplingFilter
import com.avito.instrumentation.suite.filter.FileTestsFilter
import com.avito.instrumentation.suite.filter.IgnoredAnnotationFilter
import com.avito.instrumentation.suite.filter.NameTestsFilter
import com.avito.instrumentation.suite.filter.OnlyFailedTestsFilter
import com.avito.instrumentation.suite.filter.PackagePrefixFilter
import com.avito.instrumentation.suite.filter.SkipSdkFilter
import com.avito.instrumentation.suite.filter.TestRunFilter
import com.avito.instrumentation.suite.model.TestWithTarget
import com.avito.report.model.DeviceName
import com.avito.report.model.SimpleRunTest
import org.funktionale.tries.Try
import java.io.File

/**
 * todo MBS-8045 сделать подмножество конфига для фильтров, сейчас тащим сюда огромный объект InstrumentationTestsAction.Params
 */
interface TestSuiteProvider {

    fun getInitialTestSuite(
        testApk: File,
        params: InstrumentationTestsAction.Params,
        testSignatureCheck: TestSignatureCheck? = null,
        previousRun: () -> Try<List<SimpleRunTest>>
    ): List<TestWithTarget>

    fun getFailedOnlySuite(
        testApk: File,
        params: InstrumentationTestsAction.Params,
        previousRun: () -> List<SimpleRunTest>
    ): List<TestWithTarget>

    class Impl(
        private val report: Report,
        private val testSuiteLoader: TestSuiteLoader = TestSuiteLoaderImpl()
    ) : TestSuiteProvider {

        private data class TestSuite(
            val testsToRun: List<TestWithTarget>,
            val skippedTests: List<Pair<TestWithTarget, TestRunFilter.Verdict.Skip>>
        )

        override fun getInitialTestSuite(
            testApk: File,
            params: InstrumentationTestsAction.Params,
            testSignatureCheck: TestSignatureCheck?,
            previousRun: () -> Try<List<SimpleRunTest>>
        ): List<TestWithTarget> {

            val suite = getTestSuite(
                targets = params.instrumentationConfiguration.targets,
                loadedTests = testSuiteLoader.loadTestSuite(testApk, testSignatureCheck),
                filters = initialFilters(params, previousRun)
            )

            if (params.instrumentationConfiguration.reportSkippedTests) {
                report.sendSkippedTests(
                    skippedTests = suite.skippedTests.map { (targetTest, verdict) ->
                        targetTest.test to verdict
                    }
                )
            }

            return suite.testsToRun
        }

        override fun getFailedOnlySuite(
            testApk: File,
            params: InstrumentationTestsAction.Params,
            previousRun: () -> List<SimpleRunTest>
        ): List<TestWithTarget> {
            return getTestSuite(
                targets = params.instrumentationConfiguration.targets,
                loadedTests = testSuiteLoader.loadTestSuite(testApk),
                filters = listOf(OnlyFailedTestsFilter(previousRun.invoke()))
            ).testsToRun
        }

        /**
         * Определяем какие тесты должны быть запущены последующим таском-раннером
         * Помимо разнообразных фильтров мы еще должны убедиться, что запрашиваемые тесты вообще находятся в тестовой apk,
         * для этого парсим dex, при помощи [TestSuiteLoader]
         *
         * TODO импакт анализ должен передавать тесты которые нужно НЕ запускать (сейчас наоборот)
         */
        private fun getTestSuite(
            targets: List<TargetConfiguration.Data>,
            loadedTests: List<TestInApk>,
            filters: List<TestRunFilter>
        ): TestSuite {
            val compositeTestRunFilter = CompositeTestRunFilter(filters)

            val source = targets.flatMap { target ->
                val deviceName = DeviceName(target.deviceName)
                loadedTests.map { testInApk ->
                    TestWithTarget(
                        test = parseTest(testInApk, deviceName),
                        target = target
                    ) to compositeTestRunFilter.runNeeded(
                        test = testInApk,
                        deviceName = deviceName,
                        api = target.reservation.device.api
                    )
                }
            }

            val skippedTests =
                source.filter { (_, verdict) -> verdict is TestRunFilter.Verdict.Skip }
                    .map { (test, verdict) -> test to verdict as TestRunFilter.Verdict.Skip }

            val testsToRun = source.filter { (_, verdict) -> verdict is TestRunFilter.Verdict.Run }
                .map { (test, _) -> test }

            return TestSuite(
                testsToRun = testsToRun,
                skippedTests = skippedTests
            )
        }

        /**
         * порядок фильтров не должен влиять на корректность
         * todo как быть с manual?
         * todo вместо params должен быть какойто верхнеуровневый конфиг
         */
        private fun initialFilters(
            params: InstrumentationTestsAction.Params,
            previousRun: () -> Try<List<SimpleRunTest>>
        ): List<TestRunFilter> {

            val filters: MutableList<TestRunFilter> = mutableListOf(
                IgnoredAnnotationFilter(),
                SkipSdkFilter()
            )

            if (params.instrumentationConfiguration.tests != null) {
                filters.add(NameTestsFilter(params.instrumentationConfiguration.tests))
            }

            if (params.instrumentationConfiguration.annotatedWith != null) {
                filters.add(AnnotatedWithFilter(params.instrumentationConfiguration.annotatedWith))
            }

            if (params.instrumentationConfiguration.tests != null) {
                filters.add(AnnotatedWithFilter(params.instrumentationConfiguration.tests))
            }

            if (params.instrumentationConfiguration.prefixFilter != null) {
                filters.add(PackagePrefixFilter(params.instrumentationConfiguration.prefixFilter))
            }

            if (params.impactAnalysisResult != null) {
                filters.add(FileTestsFilter(params.impactAnalysisResult))
            }

            if (params.downsamplingFactor != null) {
                filters.add(
                    DownsamplingFilter(
                        params.downsamplingFactor,
                        seed = System.currentTimeMillis()
                    )
                )
            }

            if (params.instrumentationConfiguration.rerunFailedTests) {
                previousRun.invoke().onSuccess { results -> filters.add(OnlyFailedTestsFilter(results)) }
            }

            return filters
        }
    }
}
