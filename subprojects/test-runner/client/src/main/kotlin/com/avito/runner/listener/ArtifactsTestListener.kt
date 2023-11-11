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
import com.avito.runner.service.worker.device.adb.AlwaysSuccessPullValidator
import com.avito.runner.service.worker.device.adb.PullValidator
import com.avito.test.model.TestCase
import com.avito.utils.deleteRecursively
import java.io.File
import java.nio.file.Path
import kotlin.io.path.createTempDirectory

internal class ArtifactsTestListener(
    private val lifecycleListener: TestLifecycleListener,
    private val outputDirectory: File,
    private val macrobenchmarkOutputDirectory: File?,
    private val saveTestArtifactsToOutputs: Boolean,
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

            is Failed.InRun, Passed.Regular -> handleFinishedTest(
                device = device,
                artifactsDir = testArtifactsDir,
                tempDirectory = tempDirectory,
            )
            is Passed.WithMacrobenchmarkOutputs -> handleFinishedTest(
                device = device,
                artifactsDir = testArtifactsDir,
                tempDirectory = tempDirectory,
                macrobenchmarkOutputs = result.outputFiles,
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
        tempDirectory: Path,
        macrobenchmarkOutputs: List<Path> = emptyList()
    ): TestResult {
        return artifactsDir
            .flatMap { dir ->
                pullArtifacts(device, dir, tempDirectory)
            }
            .combine(pullMacrobenchmarkOutputs(macrobenchmarkOutputs, device, tempDirectory)) { _, _ ->
                tempDirectory.toFile()
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

    private fun pullArtifacts(
        device: Device,
        artifactsDir: File,
        tempDirectory: Path
    ): Result<File> {
        return device.pullFile(
            deviceFile = artifactsDir.resolve("report.json").toPath(),
            hostDir = tempDirectory,
            validator = reportArtifactsPullValidator
        )
    }

    private fun pullMacrobenchmarkOutputs(
        outputs: List<Path>,
        device: Device,
        tempDirectory: Path
    ): Result<File> {
        val outputFile = outputs.firstOrNull()
            ?: return Result.Success(tempDirectory.toFile())

        val hostDirectory = macrobenchmarkOutputDirectory?.toPath()
            ?: outputDirectory.toPath()
        return device.pullFile(
            deviceFile = outputFile,
            hostDir = hostDirectory,
            validator = AlwaysSuccessPullValidator
        )
    }

    private fun handleIncompleteTest(
        result: InfrastructureError,
        device: Device,
        tempDirectory: File
    ): TestResult.Incomplete {
        val logcatResult = device.logcat(null)
        if (saveTestArtifactsToOutputs) {
            logcatResult.onSuccess {
                File(tempDirectory, "logcat.txt").writeText(it)
            }
        }
        return TestResult.Incomplete(
            infraError = result,
            logcat = logcatResult
        )
    }
}
