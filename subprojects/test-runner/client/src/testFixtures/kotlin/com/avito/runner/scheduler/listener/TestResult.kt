package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.runner.service.model.TestCaseRun
import java.io.File

fun TestResult.Companion.success(resultsDir: File = File(".")) =
    TestResult.Complete(Result.Success(resultsDir))

fun TestResult.Companion.timeout(exceptionMessage: String = "timeout") =
    TestResult.Incomplete(TestCaseRun.Result.Failed.InfrastructureError.Timeout(RuntimeException(exceptionMessage)))
