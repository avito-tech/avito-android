package com.avito.instrumentation.internal.report.listener

import com.avito.instrumentation.internal.logcat.LogcatAccessor

internal class FailingLogcatProcessor : LogcatProcessor {

    override suspend fun process(logcatAccessor: LogcatAccessor, isUploadNeeded: Boolean): String {
        return "logcat not available"
    }
}
