package com.avito.runner.listener

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.runner.model.TestCaseRun
import com.avito.runner.model.TestCaseRun.Result.Failed
import com.avito.runner.model.TestCaseRun.Result.Failed.InfrastructureError
import com.avito.runner.model.TestCaseRun.Result.Ignored
import com.avito.runner.model.TestCaseRun.Result.Passed
import com.avito.runner.model.TestResult
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.worker.device.Device
import com.avito.runner.service.worker.device.adb.PullValidator
import com.avito.test.model.TestCase
import com.avito.utils.deleteRecursively
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

internal class ArtifactsTestListener(
    private val lifecycleListener: TestLifecycleListener,
    private val outputDirectory: File,
    private val saveTestArtifactsToOutputs: Boolean,
    private val fetchLogcatForIncompleteTests: Boolean,
    private val reportArtifactsPullValidator: PullValidator,
    loggerFactory: LoggerFactory,
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
            deviceId = device.coordinate.serial.toString(),
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
        executionNumber: Int,
        testArtifactsDir: Result<File>
    ) {
        val tempDirectory = if (saveTestArtifactsToOutputs) {
            File(outputDirectory, "test-artifacts").apply {
                if (!exists()) {
                    parentFile.mkdirs()
                    require(mkdir()) { "can't mkdir $this" }
                }
            }.toPath()
        } else {
            createTempDirectory()
        }

        logger.info("Pulling artifacts to $tempDirectory")

        val testResult = when (result) {

            Passed, is Failed.InRun -> handleFinishedTest(
                device = device,
                artifactsDir = testArtifactsDir,
                tempDirectory = tempDirectory
            )

            is InfrastructureError -> handleIncompleteTest(
                result = result,
                device = device,
                tempDirectory = tempDirectory.toFile()
            )

            Ignored ->
                TestResult.Incomplete(
                    InfrastructureError.Unexpected(
                        IllegalStateException("Instrumentation executed Ignored test")
                    ),
                    logcat = Result.Failure(
                        RuntimeException("Logcat is not needed for ignored test case")
                    )
                )
        }

        lifecycleListener.finished(
            result = testResult,
            test = test,
            executionNumber = executionNumber,
        )

        if (!saveTestArtifactsToOutputs) {
            tempDirectory.deleteRecursively().onFailure { error ->
                logger.warn("Can't clear temp directory: $tempDirectory", error)
            }
        }
    }

    private fun handleFinishedTest(
        device: Device,
        artifactsDir: Result<File>,
        tempDirectory: Path
    ): TestResult {
        return artifactsDir
            .flatMap { dir ->
                device.pullDir(
                    deviceDir = dir.toPath(),
                    hostDir = tempDirectory,
                    validator = reportArtifactsPullValidator
                )
            }
            .fold(
                onSuccess = { dir ->
                    TestResult.Complete(dir)
                },
                onFailure = { throwable ->
                    handleIncompleteTest(
                        result = InfrastructureError.FailOnPullingArtifacts(throwable),
                        device = device,
                        tempDirectory = tempDirectory.toFile()
                    )
                }
            )
    }

    private fun handleIncompleteTest(
        result: InfrastructureError,
        device: Device,
        tempDirectory: File
    ): TestResult.Incomplete {
        return TestResult.Incomplete(
            infraError = result,
            logcat = if (fetchLogcatForIncompleteTests) {
                val logcatResult = device.logcat(null)
                if (saveTestArtifactsToOutputs) {
                    logcatResult.onSuccess {
                        File(tempDirectory, "logcat.txt").writeText(it)
                    }
                }
                logcatResult
            } else {
                Result.Failure(
                    RuntimeException("fetchLogcatForIncompleteTests is disabled in config")
                )
            }
        )
    }
}
