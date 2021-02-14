package com.avito.android.build_checks.internal

internal sealed class CheckResult {
    object Ok : CheckResult()
    class Failed(val message: String) : CheckResult()
}
