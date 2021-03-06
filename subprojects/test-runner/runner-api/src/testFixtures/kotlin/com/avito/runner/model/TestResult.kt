package com.avito.runner.model

import com.avito.android.Result
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

@OptIn(ExperimentalPathApi::class)
public fun TestResult.Companion.success(
    resultsDir: File = createTempDirectory("results").toFile()
): TestResult.Complete =
    TestResult.Complete(resultsDir)

public fun TestResult.Companion.timeout(
    timeoutMin: Long = 5,
    exceptionMessage: String = "timeout"
): TestResult.Incomplete =
    TestResult.Incomplete(
        TestCaseRun.Result.Failed.InfrastructureError.Timeout(
            timeoutMin = timeoutMin,
            error = RuntimeException(exceptionMessage)
        ),
        logcat = Result.Failure(IllegalStateException("stub"))
    )
