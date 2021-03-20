package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
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
        val tempDirectory = createTempDirectory()
        val testMetadataDirectory = testMetadataFullDirectory(
            targetPackage = targetPackage,
            test = test
        )

        val artifacts = try {
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

        lifecycleListener.finished(
            artifacts = artifacts,
            test = test,
            executionNumber = executionNumber
        )
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
