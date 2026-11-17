package com.avito.runner.scheduler.suite.filter

import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.suite.config.InstrumentationFilterData
import com.avito.runner.scheduler.suite.config.RunStatus
import com.avito.runner.scheduler.suite.filter.FilterFactory.Companion.JUNIT_IGNORE_ANNOTATION
import com.avito.runner.scheduler.suite.filter.TestsFilter.Signatures.TestSignature
import com.avito.runner.scheduler.suite.filter.run_results_provider.RunResultsProvider
import com.avito.test.model.TestCase

internal class FilterFactoryImpl(
    private val filterData: InstrumentationFilterData,
    private val changedTestsFilterInput: ChangedTestsFilterInput,
    private val runResultsProvider: RunResultsProvider,
    loggerFactory: LoggerFactory
) : FilterFactory {

    private val logger = loggerFactory.create<FilterFactoryImpl>()

    override fun createFilter(): TestsFilter {
        val filters = mutableListOf<TestsFilter>()
        val targetFilterMode = changedTestsFilterInput.targetFilterMode
        if (targetFilterMode is TargetFilterMode.ByTarget) {
            filters.add(
                ChangedTestsByTargetFilter(targetFilterMode.modesByTarget, changedTestsFilterInput.changedTests)
            )
        }
        filters.add(ExcludeBySkipOnSdkFilter())
        filters.addFlakyFilter()
        filters.addAnnotationFilters()
        filters.addSourceCodeSignaturesFilters()
        filters.addSourcePreviousSignatureFilters()
        filters.addSourceReportSignatureFilters()
        filters.addChangedTestsFilter()
        return CompositionFilter(filters)
    }

    private fun MutableList<TestsFilter>.addFlakyFilter() {
        if (filterData.fromSource.excludeFlaky) {
            add(ExcludeByFlakyFilter())
        }
    }

    private fun MutableList<TestsFilter>.addAnnotationFilters() {
        if (filterData.fromSource.annotations.included.isNotEmpty()) {
            add(
                IncludeAnnotationsFilter(
                    filterData.fromSource.annotations.included
                )
            )
        }
        add(
            ExcludeAnnotationsFilter(
                filterData.fromSource.annotations.excluded + JUNIT_IGNORE_ANNOTATION
            )
        )
    }

    private fun MutableList<TestsFilter>.addSourceCodeSignaturesFilters() {
        val prefixes = filterData.fromSource.prefixes
        if (prefixes.included.isNotEmpty()) {
            add(
                IncludeByTestSignaturesFilter(
                    source = TestsFilter.Signatures.Source.Code,
                    signatures = prefixes.included
                        .map {
                            TestSignature(
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
                            TestSignature(
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

            runResultsProvider.getPreviousRunsResults()
                .fold(
                    onSuccess = { previousRunTests ->
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
                    },
                    onFailure = { throwable ->
                        logger.info("Can't get tests from previous run: ${throwable.message}")
                    }
                )
        }
    }

    private fun MutableList<TestsFilter>.addSourceReportSignatureFilters() {
        val reportFilter = filterData.fromRunHistory.reportFilter
        if (reportFilter != null
            && (reportFilter.statuses.included.isNotEmpty()
                || reportFilter.statuses.excluded.isNotEmpty())
        ) {
            val statuses = reportFilter.statuses

            runResultsProvider.getRunResultsById(reportFilter.reportId)
                .fold(
                    onSuccess = { previousRunTests ->
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
                    },
                    onFailure = { throwable ->
                        logger.info("Can't get tests from source report: ${throwable.message}")
                    }
                )
        }
    }

    private fun Map<TestCase, RunStatus>.filterBy(statuses: Set<RunStatus>): Set<TestSignature> {
        return asSequence()
            .filter { (_, status) -> statuses.contains(status) }
            .map { (testCase, _) ->
                TestSignature(
                    name = testCase.name.name,
                    deviceName = testCase.deviceName.name
                )
            }.toSet()
    }

    private fun MutableList<TestsFilter>.addChangedTestsFilter() {
        val targetFilterMode = changedTestsFilterInput.targetFilterMode
        if (targetFilterMode !is TargetFilterMode.Uniform) return
        when (targetFilterMode.mode) {
            ChangedTestsFilterMode.ALL -> {
                // empty
            }
            ChangedTestsFilterMode.CHANGED -> includeChangedTests(changedTestsFilterInput.changedTests)
            ChangedTestsFilterMode.ALL_EXCEPT_CHANGED -> excludeChangedTests(changedTestsFilterInput.changedTests)
        }
    }

    private fun MutableList<TestsFilter>.includeChangedTests(tests: List<String>) {
        add(
            IncludeByTestSignaturesFilter(
                source = TestsFilter.Signatures.Source.ChangedTests,
                signatures = tests.map { name ->
                    TestSignature(
                        name = name
                    )
                }.toSet()
            )
        )
    }

    private fun MutableList<TestsFilter>.excludeChangedTests(tests: List<String>) {
        add(
            ExcludeByTestSignaturesFilter(
                source = TestsFilter.Signatures.Source.ChangedTests,
                signatures = tests.map { name ->
                    TestSignature(
                        name = name
                    )
                }.toSet()
            )
        )
    }
}
