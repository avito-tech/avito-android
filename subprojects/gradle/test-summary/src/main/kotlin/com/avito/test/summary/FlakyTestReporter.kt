package com.avito.test.summary

import com.avito.android.Result

internal interface FlakyTestReporter {
    fun reportSummary(
        info: List<FlakyInfo>
    ): Result<Unit>
}
