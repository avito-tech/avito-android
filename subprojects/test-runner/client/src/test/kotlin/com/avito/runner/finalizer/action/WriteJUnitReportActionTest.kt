package com.avito.runner.finalizer.action

import com.avito.report.NoOpReportLinkGenerator
import com.avito.report.NoOpTestSuiteNameProvider
import com.avito.report.model.AndroidTest
import com.avito.report.model.Incident
import com.avito.report.model.IncidentElement
import com.avito.report.model.TestRuntimeDataPackage
import com.avito.report.model.TestStaticDataPackage
import com.avito.report.model.createStubInstance
import com.avito.runner.finalizer.verdict.Verdict
import com.avito.test.model.TestName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class WriteJUnitReportActionTest {

    private lateinit var file: File

    private val reportViewerUrl = "https://report/"

    @BeforeEach
    fun setup(@TempDir temp: Path) {
        file = File(temp.toFile(), "sample_report.xml")
    }

    @Test
    fun `junit report - contains caseId data`() {
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Completed.createStubInstance(
                        testStaticData = TestStaticDataPackage.createStubInstance(
                            testCaseId = 8888
                        )
                    )
                )
            )
        )
        val rawFile = file.readText()
        assertThat(rawFile).contains("caseId=\"8888\"")
    }

    @Test
    fun `junit report - contains skipped case`() {
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Skipped.createStubInstance()
                )
            )
        )
        val rawFile = file.readText()
        assertThat(rawFile).contains("<skipped/>")
    }

    @Test
    fun `junit report - contains failure data with report viewer link`() {
        val failed = AndroidTest.Completed.createStubInstance(
            testStaticData = TestStaticDataPackage.createStubInstance(),
            testRuntimeData = TestRuntimeDataPackage.createStubInstance(
                incident = Incident.createStubInstance(
                    chain = listOf(
                        IncidentElement(message = "Something went wrong")
                    )
                )
            )
        )

        mockData(
            Verdict.Failure(
                testResults = listOf(failed),
                notReportedTests = emptyList(),
                unsuppressedFailedTests = listOf(failed)
            )
        )
        val rawFile = file.readText()
        assertThat(rawFile).contains("<failure>\nSomething went wrong\n$reportViewerUrl\n</failure>")
    }

    @Test
    fun `junit report - contains test class and method names`() {
        mockData(
            Verdict.Success.OK(
                testResults = listOf(
                    AndroidTest.Completed.createStubInstance(
                        testStaticData = TestStaticDataPackage.createStubInstance(
                            name = TestName(
                                "com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest",
                                "resolve_advert_legacyFormat"
                            )
                        )
                    )
                )
            )
        )
        val rawFile = file.readText()
        assertThat(rawFile).contains("classname=\"com.avito.android.deep_linking.DeepLinkingActivityIntentFilterTest\"")
        assertThat(rawFile).contains("name=\"resolve_advert_legacyFormat\"")
    }

    private fun mockData(verdict: Verdict) {
        WriteJUnitReportAction(
            destination = file,
            testSuiteNameProvider = NoOpTestSuiteNameProvider(),
            reportLinkGenerator = NoOpReportLinkGenerator(testLink = reportViewerUrl)
        ).action(
            verdict = verdict
        )
    }
}
