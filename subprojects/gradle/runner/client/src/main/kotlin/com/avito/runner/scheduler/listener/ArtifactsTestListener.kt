package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.scheduler.listener.TestLifecycleListener.TestResult
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.TestCaseRun.Result.Failed
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError
import com.avito.runner.service.model.TestCaseRun.Result.Ignored
import com.avito.runner.service.model.TestCaseRun.Result.Passed
import com.avito.runner.service.worker.device.Device
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

internal class ArtifactsTestListener(
    private val lifecycleListener: TestLifecycleListener,
    loggerFactory: LoggerFactory
) : TestListener {

    private val logger = loggerFactory.create<ArtifactsTestListener>()

    override fun started(
        device: Device,
        targetPackage: String,
        test: TestCase,
        executionNumber: Int
    ) {
        lifecycleListener.started(
            test = test,
            device = device,
            executionNumber = executionNumber
        )
    }

    @ExperimentalPathApi
    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        val testResult = when (result) {
            Passed,
            is Failed.InRun ->
                pullArtifacts(device, test, targetPackage)
            is InfrastructureError ->
                TestResult.Incomplete(result)
            Ignored ->
                TestResult.Incomplete(
                    InfrastructureError.Unexpected(
                        IllegalStateException("Instrumentation executed Ignored test")
                    )
                )
        }

        lifecycleListener.finished(
            result = testResult,
            test = test,
            executionNumber = executionNumber,
        )
    }

    @ExperimentalPathApi
    private fun pullArtifacts(
        device: Device,
        test: TestCase,
        targetPackage: String
    ): TestResult.Complete {
        val tempDirectory = createTempDirectory()
        val artifacts = try {
            val testMetadataDirectory = testMetadataFullDirectory(
                targetPackage = targetPackage,
                test = test
            )
            val pullingResult = device.pull(
                from = testMetadataDirectory.toPath(),
                to = tempDirectory
            )

            val resultDirectory = pullingResult
                .map {
                    File(
                        tempDirectory.toFile(),
                        testMetadataDirectoryPath(test)
                    )
                }

            device.clearDirectory(
                remotePath = testMetadataDirectory.toPath()
            ).getOrThrow()

            resultDirectory
        } catch (t: Throwable) {
            logger.warn("Failed to process artifacts from $device", t)
            Result.Failure(t)
        } finally {
            tempDirectory.toFile().delete()
        }
        return TestResult.Complete(artifacts)
    }

    @Suppress("SdCardPath") // android API's are unavailable here
    private fun testMetadataFullDirectory(targetPackage: String, test: TestCase): File =
        File("/sdcard/Android/data/$targetPackage/files/$RUNNER_OUTPUT_FOLDER/${testMetadataDirectoryPath(test)}")

    private fun testMetadataDirectoryPath(test: TestCase): String = "${test.className}#${test.methodName}"

    companion object {
        // todo should be passed with instrumentation params, see [ExternalStorageTransport]
        private const val RUNNER_OUTPUT_FOLDER = "runner"
    }
}
