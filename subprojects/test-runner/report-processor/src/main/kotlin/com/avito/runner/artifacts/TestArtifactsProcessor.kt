package com.avito.runner.artifacts

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.TestStaticData
import com.avito.runner.logcat.LogcatAccessor
import java.io.File

internal interface TestArtifactsProcessor {

    fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatAccessor: LogcatAccessor
    ): Result<AndroidTest>
}
