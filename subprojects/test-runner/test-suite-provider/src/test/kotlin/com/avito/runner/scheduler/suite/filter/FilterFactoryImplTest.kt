package com.avito.runner.scheduler.suite.filter

import com.avito.android.Result
import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.runner.scheduler.suite.config.createStub
import com.avito.runner.scheduler.suite.filter.TestsFilter.Signatures.Source
import com.avito.runner.scheduler.suite.filter.TestsFilter.Signatures.TestSignature
import com.avito.test.model.DeviceName
import com.avito.test.model.TestCase
import com.avito.test.model.TestName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class FilterFactoryImplTest {

    @Test
    fun `when filterData is empty then filters always contains ExcludedBySdk and ExcludeAnnotationFilter`() {
        val factory = StubFilterFactoryFactory.create()

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
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
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
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
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
    fun `when filterData includePrevious statuses and Report returns list without that status then filters contain IncludeTestSignaturesFilters#Previous with empty signatures`() {
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Failed),
                    excluded = emptySet()
                )
            )
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

    @Test
    fun `when filterData - includePrevious statuses and Report failed - then filters contain defaults`() {
        val runResultsProvider = StubRunResultsProvider()
        runResultsProvider.previousRunResults = Result.Failure(IllegalStateException("something went wrong"))

        val factory = StubFilterFactoryFactory.create(
            runResultsProvider = runResultsProvider,
            filter = InstrumentationFilterData.createStub(
                previousStatuses = Filter.Value(
                    included = setOf(RunStatus.Success),
                    excluded = emptySet()
                )
            )
        )

        val filter = factory.createFilter() as CompositionFilter

        val that = assertThat(filter.filters)

        that.containsAtLeastElementsIn(
            listOf(
                ExcludeBySkipOnSdkFilter(),
                ExcludeAnnotationsFilter(setOf(FilterFactory.JUNIT_IGNORE_ANNOTATION))
            )
        )
    }

    @Test
    fun `when filterData previousStatuses is empty then filters don't contain PreviousRun filters`() {
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(
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
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub()
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
    fun `when filterData report is present and statuses empty then filters don't contain Report filter`() {
        val runResultsProvider = StubRunResultsProvider()
        val reportId = "report#1"
        runResultsProvider.reportIdToRunResults = Result.Success(
            mapOf(
                reportId to mapOf(
                    TestCase(TestName("", "test1"), DeviceName("25")) to RunStatus.Success,
                    TestCase(TestName("", "test2"), DeviceName("25")) to RunStatus.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            runResultsProvider = runResultsProvider,
            filter = InstrumentationFilterData.createStub(
                report = InstrumentationFilterData.FromRunHistory.ReportFilter(
                    reportId = reportId,
                    statuses = Filter.Value(
                        included = emptySet(),
                        excluded = emptySet()
                    )
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
    fun `when report for required id is not present then filters don't contain Report filter`() {
        val runResultsProvider = StubRunResultsProvider()
        runResultsProvider.reportIdToRunResults = Result.Success(
            mapOf(
                "report#1" to mapOf(
                    TestCase(TestName("", "test1"), DeviceName("25")) to RunStatus.Success,
                    TestCase(TestName("", "test2"), DeviceName("25")) to RunStatus.Lost
                )
            )
        )

        val factory = StubFilterFactoryFactory.create(
            runResultsProvider = runResultsProvider,
            filter = InstrumentationFilterData.createStub(
                report = InstrumentationFilterData.FromRunHistory.ReportFilter(
                    reportId = "report#2",
                    statuses = Filter.Value(
                        included = setOf(RunStatus.Success),
                        excluded = setOf(RunStatus.Lost)
                    )
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
    fun `filterData is empty and changedTest is not empty - contains impact analysis filter`() {
        val factory = StubFilterFactoryFactory.create(
            filter = InstrumentationFilterData.createStub(),
            impactAnalysisResult = ImpactAnalysisResult.createStubInstance(
                runOnlyChangedTests = false,
                changedTests = listOf("com.stub.Test")
            )
        )

        val compositionFilter = factory.createFilter() as CompositionFilter

        assertThat(compositionFilter.filters).contains(
            ExcludeByTestSignaturesFilter(
                source = Source.ImpactAnalysis,
                signatures = setOf(
                    TestSignature(
                        name = "com.stub.Test",
                    )
                )
            )
        )
    }
}
