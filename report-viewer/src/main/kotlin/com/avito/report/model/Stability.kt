package com.avito.report.model

sealed class Stability(val attemptsCount: Int, val successCount: Int) {
    object Stable : Stability(1, 1)
    class Flaky(attemptsCount: Int, successCount: Int) : Stability(attemptsCount, successCount)
    class Failing(attemptsCount: Int) : Stability(attemptsCount, 0)
}
