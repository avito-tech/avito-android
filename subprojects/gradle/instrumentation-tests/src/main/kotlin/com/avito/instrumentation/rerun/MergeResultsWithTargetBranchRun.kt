package com.avito.instrumentation.rerun

import com.avito.report.ReportsApi
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.Status
import com.avito.utils.logging.CILogger
import org.funktionale.tries.Try

class MergeResultsWithTargetBranchRun(
    private val reports: ReportsApi,
    private val logger: CILogger,
    private val mainReportCoordinates: ReportCoordinates
) {

    private val tag = "[MergeResultsWithTargetBranchRun]"

    fun merge(
        initialRunResults: Try<List<SimpleRunTest>>,
        rerunResults: Try<List<SimpleRunTest>>
    ): Try<List<SimpleRunTest>> {

        //todo тест что будет если rerun results failure
        return initialRunResults
            .flatMap { initial: List<SimpleRunTest> ->
                rerunResults
                    .map { rerun: List<SimpleRunTest> ->
                        val pairs = findPairs(initial, rerun)
                        pairs.map { (firstRun, rerun) -> mergeResults(firstRun, rerun) }
                    }
                    .flatMap { markedAsSuccessful: List<Boolean> ->
                        if (markedAsSuccessful.any()) {
                            reports.getTestsForRunId(reportCoordinates = mainReportCoordinates)
                        } else {
                            initialRunResults
                        }
                    }
            }
    }

    /**
     * Находим пару каждому тесту из первого прогона в перезапуске
     * null(не найден) нужен, чтобы потом потом в логе распечатать
     */
    private fun findPairs(
        initialRun: List<SimpleRunTest>,
        rerun: List<SimpleRunTest>
    ): List<Pair<SimpleRunTest, SimpleRunTest?>> {
        return initialRun.map { it to rerun.find { rerun -> isSameTest(it, rerun) } }
    }

    private fun isSameTest(first: SimpleRunTest, second: SimpleRunTest): Boolean {
        return first.name == second.name && first.deviceName == second.deviceName
    }

    /**
     * @return true если тест помечен как успешный
     */
    private fun mergeResults(firstRun: SimpleRunTest, rerun: SimpleRunTest?): Boolean {
        var markedAsSuccessful = false
        val testName = firstRun.name
        when {
            rerun == null -> {
                //todo add failed and print only inconsistent
            }

            firstRun.status::class.java != rerun.status::class.java ->
                logger.info("$tag $testName is ${firstRun.status} while $rerun is ${rerun.status}")

            firstRun.status is Status.Failure && rerun.status is Status.Failure -> {
                val firstStatus = firstRun.status as Status.Failure
                val rerunStatus = rerun.status as Status.Failure

                val message = if (firstStatus.errorHash == rerunStatus.errorHash) {
                    "$testName error hashes are equal"
                } else {
                    "$testName error hashes are different\n---original:--------\n${firstStatus.verdict}\n---rerun:-----------\n${rerunStatus.verdict}"
                }

                logger.info("$tag $message")

                markAsSuccessful(firstRun.id, message)
                    .fold(
                        {
                            markedAsSuccessful = true
                            logger.info("$tag $testName marked as successful")
                        },
                        { exception -> logger.info("$tag cant mark $testName as successful", exception) }
                    )
            }

            else -> logger.info("$tag unexpected statuses: $testName is ${firstRun.status}, $rerun is ${rerun.status}")
        }
        return markedAsSuccessful
    }

    private fun markAsSuccessful(id: String, message: String) = reports.markAsSuccessful(
        testRunId = id,
        author = "Roberto",
        comment = message
    )
}
