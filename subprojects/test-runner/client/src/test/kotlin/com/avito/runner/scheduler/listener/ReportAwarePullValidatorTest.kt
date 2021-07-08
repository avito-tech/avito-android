package com.avito.runner.scheduler.listener

import com.avito.report.TestArtifactsProviderFactory
import com.avito.runner.listener.ReportAwarePullValidator
import com.avito.runner.service.worker.device.adb.PullValidator
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi

@ExperimentalPathApi
internal class ReportAwarePullValidatorTest {

    @Test
    fun `validate - ok - report json is available`(@TempDir tempDir: Path) {
        val validator = ReportAwarePullValidator(TestArtifactsProviderFactory)

        File(tempDir.toFile(), "report.json").writeText("some content")

        val result = validator.isPulledCompletely(tempDir)

        assertThat(result).isInstanceOf<PullValidator.Result.Ok>()
    }

    @Test
    fun `validate - failure - report json not found`(@TempDir tempDir: Path) {
        val validator = ReportAwarePullValidator(TestArtifactsProviderFactory)

        val result = validator.isPulledCompletely(tempDir)

        assertThat(result).isInstanceOf<PullValidator.Result.Failure>()
    }
}
