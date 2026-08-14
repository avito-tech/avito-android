package com.avito.report

import com.avito.android.AnnotationData
import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.android.test.annotations.CommandUuid
import com.avito.test.model.DeviceName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class CommandUuidAnnotationParsingTest {

    private val parser = ReportViewerTestStaticDataParser.Impl(
        targets = listOf(
            ReportViewerTestStaticDataParser.TargetDevice(
                name = DeviceName("test-device"),
                api = 24
            )
        )
    )

    @Test
    fun `no annotations - parse test - returns null command uuid`() {
        val testInApk = TestInApk.createStubInstance(
            annotations = emptyList()
        )

        val result = parser.getTestSuite(listOf(testInApk))

        assertThat(result.single().testStaticData.commandUuid).isNull()
    }

    @Test
    fun `command uuid annotation - parse test - returns command uuid`() {
        val result = parser.getTestSuite(listOf(testInApkWithCommandUuid("b5a5ff6d-73ef-400e-abda-6a89b19e4729")))

        assertThat(result.single().testStaticData.commandUuid)
            .isEqualTo("b5a5ff6d-73ef-400e-abda-6a89b19e4729")
    }

    @Test
    fun `uppercase command uuid - parse test - returns lowercase command uuid`() {
        val result = parser.getTestSuite(listOf(testInApkWithCommandUuid("B5A5FF6D-73EF-400E-ABDA-6A89B19E4729")))

        assertThat(result.single().testStaticData.commandUuid)
            .isEqualTo("b5a5ff6d-73ef-400e-abda-6a89b19e4729")
    }

    @Test
    fun `command uuid with spaces - parse test - returns trimmed command uuid`() {
        val result = parser.getTestSuite(listOf(testInApkWithCommandUuid("  b5a5ff6d-73ef-400e-abda-6a89b19e4729 ")))

        assertThat(result.single().testStaticData.commandUuid)
            .isEqualTo("b5a5ff6d-73ef-400e-abda-6a89b19e4729")
    }

    @Test
    fun `blank command uuid - parse test - returns null command uuid`() {
        val result = parser.getTestSuite(listOf(testInApkWithCommandUuid("   ")))

        assertThat(result.single().testStaticData.commandUuid).isNull()
    }

    private fun testInApkWithCommandUuid(value: String): TestInApk = TestInApk.createStubInstance(
        annotations = listOf(
            AnnotationData(
                name = CommandUuid::class.java.canonicalName,
                values = mapOf("value" to value)
            )
        )
    )
}
