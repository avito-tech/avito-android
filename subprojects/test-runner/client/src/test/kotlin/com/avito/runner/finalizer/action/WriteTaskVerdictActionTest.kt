package com.avito.runner.finalizer.action

import com.avito.report.NoOpReportLinksGenerator
import com.avito.report.ReportLinksGenerator
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.runner.finalizer.verdict.Verdict
import com.avito.test.model.DeviceName
import com.avito.test.model.TestName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class WriteTaskVerdictActionTest {

    @Test
    fun `file - contains ok message - verdict is ok`(@TempDir tempDir: File) {
        val verdict = Verdict.Success.OK(testResults = emptySet())
        val action = createWriteTaskVerdictAction(tempDir)
        action.action(verdict)

        val verdictContent = readFile(tempDir)

        assertThat(verdictContent).contains("OK. No failed tests")
    }

    @Test
    fun `file - contains suppressed message - verdict is suppressed`(@TempDir tempDir: File) {
        val failedTest = AndroidTest.Completed.createStubInstance(
            testStaticData = TestStaticDataPackage.createStubInstance(
                name = TestName("com.test.Test2", "test")
            ),
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(incident = Incident.createStubInstance())
        )

        val verdict = Verdict.Success.Suppressed(
            testResults = setOf(
                AndroidTest.Completed.createStubInstance(
                    testStaticData = TestStaticDataPackage.createStubInstance(
                        name = TestName("com.test.Test1", "test")
                    )
                ),
                failedTest
            ),
            notReportedTests = emptyList(),
            failedTests = setOf(failedTest)
        )
        val action = createWriteTaskVerdictAction(tempDir)
        action.action(verdict)

        val verdictContent = readFile(tempDir)

        assertThat(verdictContent).contains("OK. Failed tests were suppressed")
    }

    @Test
    fun `file - contains fail message with test - verdict is failure`(@TempDir tempDir: File) {
        val failedTest = AndroidTest.Completed.createStubInstance(
            testStaticData = TestStaticDataPackage.createStubInstance(
                name = TestName("com.test.Test2", "test"),
                deviceName = DeviceName("API22")
            ),
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(incident = Incident.createStubInstance())
        )

        val verdict = Verdict.Failure(
            testResults = setOf(
                AndroidTest.Completed.createStubInstance(
                    testStaticData = TestStaticDataPackage.createStubInstance(
                        name = TestName("com.test.Test1", "test")
                    )
                ),
                failedTest
            ),
            unsuppressedFailedTests = setOf(failedTest),
            notReportedTests = emptyList()
        )
        val action = createWriteTaskVerdictAction(tempDir)
        action.action(verdict)

        val verdictContent = readFile(tempDir)

        assertThat(verdictContent).contains("Not suppressed failed tests: 1")
        assertThat(verdictContent).contains("com.test.Test2.test API22 FAILED")
    }

    @Test
    fun `file - contains lost message with test - verdict is failure`(@TempDir tempDir: File) {
        val lostTest = AndroidTest.Lost.createStubInstance(
            testStaticData = TestStaticDataPackage.createStubInstance(
                name = TestName("com.test.Test2", "test"),
                deviceName = DeviceName("API22")
            )
        )

        val verdict = Verdict.Failure(
            testResults = setOf(
                AndroidTest.Completed.createStubInstance(
                    testStaticData = TestStaticDataPackage.createStubInstance(
                        name = TestName("com.test.Test1", "test")
                    )
                ),
                lostTest
            ),
            unsuppressedFailedTests = emptyList(),
            notReportedTests = setOf(lostTest)
        )
        val action = createWriteTaskVerdictAction(tempDir)
        action.action(verdict)

        val verdictContent = readFile(tempDir)

        assertThat(verdictContent).contains("Not reported tests: 1")
        assertThat(verdictContent).contains("com.test.Test2.test API22 NOT REPORTED")
    }

    @Test
    fun `file - contains lost message without suppressed tests - verdict is failure`(@TempDir tempDir: File) {
        val lostTest = AndroidTest.Lost.createStubInstance(
            testStaticData = TestStaticDataPackage.createStubInstance(
                name = TestName("com.test.Test2", "test"),
                deviceName = DeviceName("API22")
            )
        )

        val verdict = Verdict.Failure(
            testResults = setOf(
                AndroidTest.Completed.createStubInstance(
                    testStaticData = TestStaticDataPackage.createStubInstance(
                        name = TestName("com.test.Test1", "test")
                    )
                ),
                lostTest
            ),
            unsuppressedFailedTests = emptyList(),
            notReportedTests = setOf(lostTest)
        )
        val action = createWriteTaskVerdictAction(tempDir)
        action.action(verdict)

        val verdictContent = readFile(tempDir)

        assertThat(verdictContent).contains("Not reported tests: 1")
        assertThat(verdictContent).contains("com.test.Test2.test API22 NOT REPORTED")
    }

    private fun createWriteTaskVerdictAction(
        tempDir: File,
        reportLinksGenerator: ReportLinksGenerator = NoOpReportLinksGenerator()
    ): WriteTaskVerdictAction {
        return WriteTaskVerdictAction(
            verdictDestination = verdictFile(tempDir),
            reportLinksGenerator = reportLinksGenerator,
        )
    }

    private fun readFile(tempDir: File): String {
        return verdictFile(tempDir).readText()
    }

    private fun verdictFile(tempDir: File): File {
        return File(tempDir, "verdict.json")
    }
}
