package com.avito.android.plugin.build_metrics.internal

internal sealed class BuildStatus {
    object Success : BuildStatus()
    object Fail : BuildStatus()
}
