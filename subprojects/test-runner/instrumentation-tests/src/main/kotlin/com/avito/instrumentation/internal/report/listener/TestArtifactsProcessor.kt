package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.instrumentation.internal.logcat.LogcatAccessor
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import java.io.File

internal interface TestArtifactsProcessor {

    fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatAccessor: LogcatAccessor
    ): Result<AndroidTest>
}
