package com.avito.emcee.worker.internal

import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal interface TestExecutor {

    data class Result(val success: Boolean)

    @ExperimentalTime
    data class Job(
        val apk: File,
        val testApk: File,
        val test: TestEntry,
        val applicationPackage: String,
        val testPackage: String,
        val instrumentationRunnerClass: String,
        val testExecutionBehavior: TestExecutionBehavior,
        val testMaximumDuration: Duration
    )

    suspend fun beforeTestBucket()
    suspend fun execute(job: Job): Result
    suspend fun afterTestBucket()
}
