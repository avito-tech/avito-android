package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.Data.FromRunHistory.ReportFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory.RunStatus
import com.avito.instrumentation.createStub
import com.avito.instrumentation.internal.suite.filter.CompositionFilter
import com.avito.instrumentation.internal.suite.filter.ExcludeAnnotationsFilter
import com.avito.instrumentation.internal.suite.filter.ExcludeBySkipOnSdkFilter
import com.avito.instrumentation.internal.suite.filter.ExcludeByTestSignaturesFilter
import com.avito.instrumentation.internal.suite.filter.FilterFactory
import com.avito.instrumentation.internal.suite.filter.IncludeAnnotationsFilter
import com.avito.instrumentation.internal.suite.filter.IncludeByTestSignaturesFilter
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Signatures.Source
import com.avito.instrumentation.internal.suite.filter.TestsFilter.Signatures.TestSignature
import com.avito.instrumentation.report.ReportFactory
import com.avito.instrumentation.stub.report.StubReport
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.report.model.createStubInstance
import com.google.common.truth.Truth.assertThat
import org.funktionale.tries.Try
import org.junit.jupiter.api.Test

internal class FilterFactoryImplTest {

    @Test
    fun `when filterData is empty then filters always contains ExcludedBySdk and ExcludeAnnotationFilter`() {
        val factory = FilterFactoryFactory.create()

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsExactly(
                ExcludeBySkipOnSdkFilter(),
                ExcludeAnnotationsFilter(setOf(FilterFactory.JUNIT_IGNORE_ANNOTATION))
            )
    }

    @Test
    fun `when filterData contains included annotations then filters have IncludeAnnotationFilter`() {
        val annotation = "Annotation"
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                annotations = Filter.Value(
                    included = setOf(annotation),
                    excluded = emptySet()
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsAtLeastElementsIn(listOf(IncludeAnnotationsFilter(setOf(annotation))))
    }

    @Test
    fun `when filterData contains prefixes then filters have IncludeBySignatures, ExcludeBySignatures`() {
        val includedPrefix = "included_prefix"
        val excludedPrefix = "excluded_prefix"
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                prefixes = Filter.Value(
                    included = setOf(includedPrefix),
                    excluded = setOf(excludedPrefix)
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsAtLeastElementsIn(
                listOf(
                    IncludeByTestSignaturesFilter(
                        source = Source.Code,
                        signatures = setOf(
                            TestSignature(
                                name = includedPrefix
                            )
                        )
                    ),
                    ExcludeByTestSignaturesFilter(
                        source = Source.Code,
                        signatures = setOf(
                            TestSignature(
                                name = excludedPrefix
                            )
                        )
                    )
                )
            )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `when filterData includePrevious statuses and Report return list without that status then filters contain IncludeTestSignaturesFilters#Previous with empty signatures`() {
        val reportConfig = ReportFactory.Config.ReportViewerCoordinates(ReportCoordinates.createStubInstance(), "stub")
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Failed),
                    excluded = emptySet()
                )
            ),
            reportsByConfig = mapOf(
                reportConfig to StubReport()
            ),
            reportConfig = reportConfig
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = assertThat(filter.filters)
        that.containsAtLeastElementsIn(
            listOf(
                IncludeByTestSignaturesFilter(
                    source = Source.PreviousRun,
                    signatures = emptySet()
                )
            )
        )
    }

    @Suppress("MaxLineLength")
    @Test
    fun `when filterData includePrevious statuses and Report return list then filters contain IncludeTestSignaturesFilters#Previous with included statuses`() {
        val report = StubReport()
        report.getTestsResult = Try.Success(
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

        val reportConfig = ReportFactory.Config.ReportViewerCoordinates(ReportCoordinates.createStubInstance(), "stub")
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Success),
                    excluded = emptySet()
                )
            ),
            reportsByConfig = mapOf(
                reportConfig to report
            ),
            reportConfig = reportConfig
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = assertThat(filter.filters)
        that.containsAtLeastElementsIn(
            listOf(
                IncludeByTestSignaturesFilter(
                    source = Source.PreviousRun,
                    signatures = setOf(
                        TestSignature(
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
        report.getTestsResult = Try.Success(
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
        val reportConfig = ReportFactory.Config.ReportViewerCoordinates(ReportCoordinates.createStubInstance(), "stub")
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                previousStatuses = Filter.Value(
                    included = emptySet(),
                    excluded = setOf(RunStatus.Success)
                )
            ),
            reportsByConfig = mapOf(
                reportConfig to report
            ),
            reportConfig = reportConfig
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = assertThat(filter.filters)
        that.containsAtLeastElementsIn(
            listOf(
                ExcludeByTestSignaturesFilter(
                    source = Source.PreviousRun,
                    signatures = setOf(
                        TestSignature(
                            name = "test1",
                            deviceName = "25"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `when filterData previousStatuses is empty then filters don't contain PreviousRun filters`() {
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                previousStatuses = Filter.Value(
                    included = emptySet(),
                    excluded = emptySet()
                )
            )
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }

    @Test
    fun `when filterData report is empty then filters don't contain Report filters`() {
        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub()
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }

    @Test
    fun `when filterData report is present and has includes then filters contain Report include filter`() {
        val reportId = "reportId"
        val reportConfig = ReportFactory.Config.ReportViewerId(reportId)
        val report = StubReport()
        report.getTestsResult = Try.Success(
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

        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                report = ReportFilter(
                    reportConfig = reportConfig,
                    statuses = Filter.Value(
                        included = setOf(RunStatus.Success),
                        excluded = emptySet()
                    )
                )
            ),
            reportsByConfig = mapOf(
                reportConfig to report
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsAtLeastElementsIn(
                listOf(
                    IncludeByTestSignaturesFilter(
                        source = Source.Report,
                        signatures = setOf(
                            TestSignature(
                                name = "test1",
                                deviceName = "25"
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `when filterData report is present and has excludes then filters contain Report exclude filter`() {
        val reportId = "reportId"
        val reportConfig = ReportFactory.Config.ReportViewerId(reportId)
        val report = StubReport()
        report.getTestsResult = Try.Success(
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

        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                report = ReportFilter(
                    reportConfig = ReportFactory.Config.ReportViewerId(reportId),
                    statuses = Filter.Value(
                        included = emptySet(),
                        excluded = setOf(RunStatus.Success)
                    )
                )
            ),
            reportsByConfig = mapOf(
                reportConfig to report
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        assertThat(filter.filters)
            .containsAtLeastElementsIn(
                listOf(
                    ExcludeByTestSignaturesFilter(
                        source = Source.Report,
                        signatures = setOf(
                            TestSignature(
                                name = "test1",
                                deviceName = "25"
                            )
                        )
                    )
                )
            )
    }

    @Test
    fun `when filterData report is present and statuses empty then filters don't contain Report filter`() {
        val reportId = "reportId"
        val reportConfig = ReportFactory.Config.ReportViewerId(reportId)
        val report = StubReport()
        report.getTestsResult = Try.Success(
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

        val factory = FilterFactoryFactory.create(
            filter = InstrumentationFilter.Data.createStub(
                report = ReportFilter(
                    reportConfig = ReportFactory.Config.ReportViewerId(reportId),
                    statuses = Filter.Value(
                        included = emptySet(),
                        excluded = emptySet()
                    )
                )
            ),
            reportsByConfig = mapOf(
                reportConfig to report
            )
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        compositionFilter.filters.forEach { filter ->
            assertThat(filter).run {
                isNotInstanceOf(IncludeByTestSignaturesFilter::class.java)
                isNotInstanceOf(ExcludeByTestSignaturesFilter::class.java)
            }
        }
    }
}
