package com.avito.report

import com.avito.android.AnnotationData
import com.avito.android.TestInApk
import com.avito.android.createStubInstance
import com.avito.android.test.annotations.Regions
import com.avito.test.model.DeviceName
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class RegionsAnnotationParsingTest {

    private val parser = ReportViewerTestStaticDataParser.Impl(
        targets = listOf(
            ReportViewerTestStaticDataParser.TargetDevice(
                name = DeviceName("test-device"),
                api = 24
            )
        )
    )

    @Test
    fun `no annotations - parse test - returns empty regions`() {
        val testInApk = TestInApk.createStubInstance(
            annotations = emptyList()
        )

        val result = parser.getTestSuite(listOf(testInApk))

        assertThat(result.single().testStaticData.regions).isEmpty()
    }

    @Test
    fun `regions annotation with values - parse test - returns regions list`() {
        val testInApk = TestInApk.createStubInstance(
            annotations = listOf(
                AnnotationData(
                    name = Regions::class.java.canonicalName,
                    values = mapOf("value" to listOf("region1", "region2", "region3"))
                )
            )
        )

        val result = parser.getTestSuite(listOf(testInApk))

        assertThat(result.single().testStaticData.regions).containsExactly("region1", "region2", "region3")
    }

    @Test
    fun `regions annotation with empty value - parse test - returns empty regions`() {
        val testInApk = TestInApk.createStubInstance(
            annotations = listOf(
                AnnotationData(
                    name = Regions::class.java.canonicalName,
                    values = mapOf("value" to emptyList<String>())
                )
            )
        )

        val result = parser.getTestSuite(listOf(testInApk))

        assertThat(result.single().testStaticData.regions).isEmpty()
    }

    @Test
    fun `regions annotation with single value - parse test - returns single region`() {
        val testInApk = TestInApk.createStubInstance(
            annotations = listOf(
                AnnotationData(
                    name = Regions::class.java.canonicalName,
                    values = mapOf("value" to listOf("region1"))
                )
            )
        )

        val result = parser.getTestSuite(listOf(testInApk))

        assertThat(result.single().testStaticData.regions).containsExactly("region1")
    }
}
