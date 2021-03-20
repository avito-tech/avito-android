package com.avito.test.summary

import com.avito.android.Result
import com.avito.report.model.SimpleRunTest
import com.avito.report.model.TestName

internal class FlakyTestInfo {

    private val info: MutableList<FlakyInfo> = mutableListOf()

    fun addReport(report: Result<List<SimpleRunTest>>) {
        if (report is Result.Success) {
            extractFlakyInfo(report.getOrThrow()).forEach { newInfo ->
                val oldInfo = info.find { it.testName == newInfo.testName }
                if (oldInfo != null) {
                    info.remove(oldInfo)
                    info.add(
                        oldInfo.copy(
                            attempts = oldInfo.attempts + newInfo.attempts,
                            wastedTimeEstimateInSec = oldInfo.wastedTimeEstimateInSec + newInfo.wastedTimeEstimateInSec
                        )
                    )
                } else {
                    info.add(newInfo)
                }
            }
        }
    }

    fun getInfo(): List<FlakyInfo> = info

    private fun extractFlakyInfo(report: List<SimpleRunTest>): List<FlakyInfo> {
        return report.filter { it.lastAttemptDurationInSeconds > 0 }
            .map {
                FlakyInfo(
                    testName = TestName(it.name),
                    attempts = it.stability.attemptsCount,
                    // предположение что проходит тест примерно за одно время, подробнее информации пока нет в api
                    wastedTimeEstimateInSec = it.lastAttemptDurationInSeconds * it.stability.attemptsCount
                )
            }
    }
}
