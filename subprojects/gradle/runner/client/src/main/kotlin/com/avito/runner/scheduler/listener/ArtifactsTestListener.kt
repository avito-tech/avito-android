package com.avito.runner.scheduler.listener

import com.avito.logger.Logger
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.worker.device.Device
import java.io.File

class ArtifactsTestListener(
    private val lifecycleListener: TestLifecycleListener,
    private val logger: Logger
) : TestListener {

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

    override fun finished(
        device: Device,
        test: TestCase,
        targetPackage: String,
        result: TestCaseRun.Result,
        durationMilliseconds: Long,
        executionNumber: Int
    ) {
        val tempDirectory = createTempDir()
        val testMetadataDirectory = testMetadataFullDirectory(
            targetPackage = targetPackage,
            test = test
        )

        try {
            val pullingResult = device.pull(
                from = testMetadataDirectory.toPath(),
                to = tempDirectory.toPath()
            )

            val resultDirectory = pullingResult
                .map {
                    File(
                        tempDirectory,
                        testMetadataDirectoryPath(test)
                    )
                }

            lifecycleListener.finished(
                artifacts = resultDirectory,
                test = test,
                executionNumber = executionNumber
            )

            device.clearDirectory(
                remotePath = testMetadataDirectory.toPath()
            ).get()
        } catch (t: Throwable) {
            logger.critical("Failed to process artifacts from device", t)
        }

        tempDirectory.delete()
    }

    private fun testMetadataFullDirectory(targetPackage: String, test: TestCase): File =
        File("/sdcard/Android/data/$targetPackage/files/runner/${testMetadataDirectoryPath(test)}")

    private fun testMetadataDirectoryPath(test: TestCase): String = "${test.className}#${test.methodName}"
}
