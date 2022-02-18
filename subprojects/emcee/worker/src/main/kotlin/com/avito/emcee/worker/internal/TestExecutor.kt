package com.avito.emcee.worker.internal

import com.avito.emcee.queue.TestEntry
import com.avito.emcee.queue.TestExecutionBehavior
import com.avito.emcee.queue.TestTimeoutConfiguration
import java.io.File

internal interface TestExecutor {

    interface Result

    data class Job(
        val apk: File,
        val testApk: File,
        val test: TestEntry,
        val applicationPackage: String,
        val testPackage: String,
        val instrumentationRunnerClass: String,
        val testExecutionBehavior: TestExecutionBehavior,
        val testTimeoutConfiguration: TestTimeoutConfiguration
    )

    suspend fun beforeTestBucket()
    suspend fun execute(job: Job): Result
    suspend fun afterTestBucket()
}
