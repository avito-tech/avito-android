package com.avito.android.plugin.build_param_check

sealed class CheckResult {
    object Ok : CheckResult()
    class Failed(val message: String) : CheckResult()
}
