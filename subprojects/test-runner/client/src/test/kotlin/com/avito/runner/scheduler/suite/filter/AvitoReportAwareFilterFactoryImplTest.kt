package com.avito.runner.scheduler.suite.filter

import com.avito.android.Result
import com.avito.android.runner.report.StubReport
import com.avito.android.runner.report.StubReportFactory
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.avito.runner.config.InstrumentationFilterData
import com.avito.runner.config.RunStatus
import com.avito.runner.config.createStub
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

/**
 * Tests that are depends on Avito Report
 * Should be extracted from FilterFactoryImpl
 */
internal class AvitoReportAwareFilterFactoryImplTest {

    @Test
    fun `when filterData report is present and has excludes then filters contain Report exclude filter`() {
        val report = StubReport()

        val reportFactory = StubReportFactory(report)

        report.getTestsResult = Result.Success(
            listOf(
                SimpleRunTest.createStubInstance(
                    name = "test1",
                    deviceName = "25",
                    status = Status.Success
                ),
                SimpleRunTest.createStubInstance(
                    name = "test2",
                    deviceName = "25",
                    status = Status.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            reportFactory = reportFactory,
            filter = InstrumentationFilterData.createStub(
                report = InstrumentationFilterData.FromRunHistory.ReportFilter(
                    statuses = Filter.Value(
                        included = emptySet(),
                        excluded = setOf(RunStatus.Success)
                    )
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        Truth.assertThat(filter.filters)
            .containsAtLeastElementsIn(
                listOf(
                    ExcludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.Report,
                        signatures = setOf(
                            TestsFilter.Signatures.TestSignature(
                                name = "test1",
                                deviceName = "25"
                            )
                        )
                    )
                )
            )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `when filterData excludePrevious statuses and Report return list then filters contain ExcludeTestSignaturesFilters#Previous with included statuses`() {
        val report = StubReport()

        val reportFactory = StubReportFactory(report)

        report.getTestsResult = Result.Success(
            listOf(
                SimpleRunTest.createStubInstance(
                    name = "test1",
                    deviceName = "25",
                    status = Status.Success
                ),
                SimpleRunTest.createStubInstance(
                    name = "test2",
                    deviceName = "25",
                    status = Status.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            reportFactory = reportFactory,
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = emptySet(),
                    excluded = setOf(RunStatus.Success)
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = Truth.assertThat(filter.filters)
        that.containsAtLeastElementsIn(
            listOf(
                ExcludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.PreviousRun,
                    signatures = setOf(
                        TestsFilter.Signatures.TestSignature(
                            name = "test1",
                            deviceName = "25"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `when filterData report is present and has includes then filters contain Report include filter`() {
        val report = StubReport()

        val reportFactory = StubReportFactory(report)

        report.getTestsResult = Result.Success(
            listOf(
                SimpleRunTest.createStubInstance(
                    name = "test1",
                    deviceName = "25",
                    status = Status.Success
                ),
                SimpleRunTest.createStubInstance(
                    name = "test2",
                    deviceName = "25",
                    status = Status.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            reportFactory = reportFactory,
            filter = InstrumentationFilterData.createStub(
                report = InstrumentationFilterData.FromRunHistory.ReportFilter(
                    statuses = Filter.Value(
                        included = setOf(RunStatus.Success),
                        excluded = emptySet()
                    )
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        Truth.assertThat(filter.filters)
            .containsAtLeastElementsIn(
                listOf(
                    IncludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.Report,
                        signatures = setOf(
                            TestsFilter.Signatures.TestSignature(
                                name = "test1",
                                deviceName = "25"
                            )
                        )
                    )
                )
            )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `when filterData includePrevious statuses and Report return list then filters contain IncludeTestSignaturesFilters#Previous with included statuses`() {
        val report = StubReport()

        val reportFactory = StubReportFactory(report)

        report.getTestsResult = Result.Success(
            listOf(
                SimpleRunTest.createStubInstance(
                    name = "test1",
                    deviceName = "25",
                    status = Status.Success
                ),
                SimpleRunTest.createStubInstance(
                    name = "test2",
                    deviceName = "25",
                    status = Status.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            reportFactory = reportFactory,
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Success),
                    excluded = emptySet()
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = Truth.assertThat(filter.filters)
        that.containsAtLeastElementsIn(
            listOf(
                IncludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.PreviousRun,
                    signatures = setOf(
                        TestsFilter.Signatures.TestSignature(
                            name = "test1",
                            deviceName = "25"
                        )
                    )
                )
            )
        )
    }
}
