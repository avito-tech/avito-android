package com.avito.runner.config

import com.avito.report.model.Status

public enum class RunStatus(public val statusClass: Class<out Status>) {
    Failed(Status.Failure::class.java),
    Success(Status.Success::class.java),
    Lost(Status.Lost::class.java),
    Skipped(Status.Skipped::class.java),
    Manual(Status.Manual::class.java)
}
