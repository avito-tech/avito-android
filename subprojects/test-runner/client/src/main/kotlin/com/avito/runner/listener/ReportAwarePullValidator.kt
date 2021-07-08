package com.avito.runner.listener

import com.avito.android.Problem
import com.avito.report.TestArtifactsProviderFactory
import com.avito.runner.service.worker.device.adb.PullValidator
import com.avito.utils.hasFileContent
import java.nio.file.Path

internal class ReportAwarePullValidator(
    private val testArtifactsProviderFactory: TestArtifactsProviderFactory
) : PullValidator {

    // todo
    //  - parse all FileAddress.File objects from report.json
    //  - validate all of them
    override fun isPulledCompletely(hostDir: Path): PullValidator.Result {
        val testArtifactsProvider = testArtifactsProviderFactory.createForTempDir(hostDir.toFile())
        return testArtifactsProvider.provideReportFile().fold(
            onSuccess = { reportJson ->
                if (reportJson.hasFileContent()) {
                    PullValidator.Result.Ok
                } else {
                    PullValidator.Result.Failure(
                        Problem(
                            shortDescription = "Report.json file not pulled",
                            context = "Checking report.json pulling from device",
                            because = "Somehow report.json not generated after test run, should be investigated",
                            possibleSolutions = listOf(
                                "Check error messages for this test, possibly generated in ExternalStorageTransport"
                            )
                        )
                    )
                }
            },
            onFailure = { throwable ->
                PullValidator.Result.Failure(
                    Problem(
                        shortDescription = "Unexpected exception getting report file path",
                        context = "Checking report.json pulling from device, getting file path",
                        because = "Never should be here",
                        throwable = throwable
                    )
                )
            }
        )
    }
}
