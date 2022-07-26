package com.avito.android.plugin.build_metrics.internal

internal fun BuildStatus.asSeriesName(): String {
    return when (this) {
        is BuildStatus.Success -> "success"
        is BuildStatus.Fail -> "fail"
    }
}
