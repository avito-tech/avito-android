package com.avito.android.build_metrics

public sealed class BuildStatus {
    public object Success : BuildStatus()
    public object Fail : BuildStatus()
}
