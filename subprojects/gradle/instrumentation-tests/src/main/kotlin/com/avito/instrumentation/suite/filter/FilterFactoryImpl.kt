package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.configuration.InstrumentationFilter
import com.avito.instrumentation.configuration.InstrumentationFilter.FromRunHistory
import com.avito.report.ReportsFetchApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import java.io.File

interface FilterFactory {
    fun createInitialFilter(): TestsFilter
    fun createRerunFilter(): TestsFilter

    companion object {
        fun create(
            filterData: InstrumentationFilter.Data,
            impactAnalysisResult: File?,
            reportCoordinates: ReportCoordinates,
            reportsFetchApi: ReportsFetchApi
        ): FilterFactory {
            return FilterFactoryImpl(
                filterData = filterData,
                impactAnalysisResult = impactAnalysisResult,
                reportCoordinates = reportCoordinates,
                reportsFetchApi = reportsFetchApi
            )
        }
    }
}

private class FilterFactoryImpl(
    private val filterData: InstrumentationFilter.Data,
    private val impactAnalysisResult: File?,
    private val reportCoordinates: ReportCoordinates,
    private val reportsFetchApi: ReportsFetchApi
) : FilterFactory {

    override fun createInitialFilter(): TestsFilter {
        val filters = mutableListOf<TestsFilter>()
        filters.add(ExcludeBySdkFilter)
        filters.addAnnotationFilters()
        filters.addSourceCodeSignaturesFilters()
        filters.addSourcePreviousSignatureFilters()
        filters.addSourceReportSignatureFilters()
        filters.addImpactAnalysisFilter()
        return CompositionFilter(
            filters
        )
    }

    override fun createRerunFilter(): TestsFilter {
        val filters = mutableListOf<TestsFilter>()
        filters.addSourcePreviousSignatureFilters()
        return CompositionFilter(
            filters
        )
    }

    private fun MutableList<TestsFilter>.addAnnotationFilters() {
        if (filterData.fromSource.annotations.included.isNotEmpty()) {
            add(
                IncludeAnnotationsFilter(
                    filterData.fromSource.annotations.included
                )
            )
        }
        if (filterData.fromSource.annotations.excluded.isNotEmpty()) {
            add(
                ExcludeAnnotationsFilter(
                    filterData.fromSource.annotations.excluded + "org.junit.Ignore"
                )
            )
        }
    }

    private fun MutableList<TestsFilter>.addSourceCodeSignaturesFilters() {
        val prefixes = filterData.fromSource.prefixes
        if (prefixes.included.isNotEmpty()) {
            add(
                IncludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.Code,
                    signatures = prefixes.included
                        .map {
                            TestsFilter.Signatures.TestSignature(
                                name = it
                            )
                        }.toSet()
                )
            )
        }
        if (prefixes.excluded.isNotEmpty()) {
            add(
                ExcludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.Code,
                    signatures = prefixes.excluded
                        .map {
                            TestsFilter.Signatures.TestSignature(
                                name = it
                            )
                        }.toSet()
                )
            )
        }
    }

    private fun MutableList<TestsFilter>.addSourcePreviousSignatureFilters() {
        val previousStatuses = filterData.fromRunHistory.previousStatuses
        if (previousStatuses.included.isNotEmpty() || previousStatuses.excluded.isNotEmpty()) {
            val previousRunTests = reportsFetchApi
                .getTestsForRunId(reportCoordinates)
                .get()
            if (previousStatuses.included.isNotEmpty()) {
                add(
                    IncludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.PreviousRun,
                        signatures = previousRunTests.filterBy(previousStatuses.included)
                    )

                )
            }
            if (previousStatuses.excluded.isNotEmpty()) {
                add(
                    ExcludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.PreviousRun,
                        signatures = previousRunTests.filterBy(previousStatuses.excluded)
                    )

                )
            }
        }
    }

    private fun MutableList<TestsFilter>.addSourceReportSignatureFilters() {
        val reportFilter = filterData.fromRunHistory.reportFilter
        if (reportFilter != null && (reportFilter.statuses.included.isNotEmpty() || reportFilter.statuses.excluded.isNotEmpty())) {
            val statuses = reportFilter.statuses
            val previousRunTests = reportsFetchApi
                .getTestsForReportId(reportFilter.id)
                .get()
            if (statuses.included.isNotEmpty()) {
                add(
                    IncludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.Report,
                        signatures = previousRunTests.filterBy(statuses.included)
                    )

                )
            }
            if (statuses.excluded.isNotEmpty()) {
                add(
                    ExcludeByTestSignaturesFilter(
                        source = TestsFilter.Signatures.Source.Report,
                        signatures = previousRunTests.filterBy(statuses.excluded)
                    )

                )
            }
        }
    }

    private fun List<SimpleRunTest>.filterBy(statuses: Set<FromRunHistory.RunStatus>): Set<TestsFilter.Signatures.TestSignature> {
        return asSequence()
            .filter { testRun -> statuses.any { it.statusClass.isInstance(testRun.status) } }
            .map { testRun ->
                TestsFilter.Signatures.TestSignature(
                    name = testRun.name,
                    deviceName = testRun.deviceName
                )
            }.toSet()
    }

    private fun MutableList<TestsFilter>.addImpactAnalysisFilter() {
        if (impactAnalysisResult != null && impactAnalysisResult.exists()) {
            val testNames = impactAnalysisResult.readLines()
            add(
                IncludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.ImpactAnalysis,
                    signatures = testNames.map { name ->
                        TestsFilter.Signatures.TestSignature(
                            name = name
                        )
                    }.toSet()
                )
            )
        }
    }

}
