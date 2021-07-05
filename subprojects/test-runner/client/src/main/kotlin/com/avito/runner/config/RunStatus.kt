package com.avito.runner.config

import com.avito.test.model.TestStatus

public enum class RunStatus(public val statusClass: Class<out TestStatus>) {
    Failed(TestStatus.Failure::class.java),
    Success(TestStatus.Success::class.java),
    Lost(TestStatus.Lost::class.java),
    Skipped(TestStatus.Skipped::class.java),
    Manual(TestStatus.Manual::class.java)
}
