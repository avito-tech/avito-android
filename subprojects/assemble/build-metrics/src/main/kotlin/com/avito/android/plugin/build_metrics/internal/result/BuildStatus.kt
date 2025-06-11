package com.avito.android.plugin.build_metrics.internal.result

internal sealed class BuildStatus {
    object Success : BuildStatus()
    object Fail : BuildStatus()
}
