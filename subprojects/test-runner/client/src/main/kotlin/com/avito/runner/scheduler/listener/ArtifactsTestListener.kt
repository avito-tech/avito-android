package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.logger.LoggerFactory
import com.avito.logger.create
import com.avito.report.TestArtifactsProviderFactory
import com.avito.runner.service.listener.TestListener
import com.avito.runner.service.model.TestCase
import com.avito.runner.service.model.TestCaseRun
import com.avito.runner.service.model.TestCaseRun.Result.Failed
import com.avito.runner.service.model.TestCaseRun.Result.Failed.InfrastructureError
import com.avito.runner.service.model.TestCaseRun.Result.Ignored
import com.avito.runner.service.model.TestCaseRun.Result.Passed
import com.avito.runner.service.worker.device.Device
import com.avito.utils.deleteRecursively
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory

internal class ArtifactsTestListener(
    private val lifecycleListener: TestLifecycleListener,
    private val outputDirectory: File,
    private val saveTestArtifactsToOutputs: Boolean,
    private val fetchLogcatForIncompleteTests: Boolean,
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

            Passed, is Failed.InRun -> testArtifactsDir.flatMap { dir ->
                device.pullDir(
                    deviceDir = dir.toPath(),
                    hostDir = tempDirectory,
                    validator = ReportAwarePullValidator(
                        testArtifactsProviderFactory = TestArtifactsProviderFactory
                    )
                )
            }.fold(
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
