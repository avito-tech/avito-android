package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.createStub
import com.avito.report.model.DeviceName
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

internal class TestSignaturesFilterTest {

    @Test
    fun `when testName equals signatureName then test will match signature`() {
        val testName = "TestClass.method"
        val test = TestsFilter.Test.createStub(testName)
        val signature = TestsFilter.Signatures.TestSignature(
            name = testName
        )
        Truth.assertThat(test.matched(setOf(signature))).isTrue()
    }

    @Test
    fun `when testName starts with signatureName then test will match signature`() {
        val className = "TestClass"
        val testName = "$className.method"
        val test = TestsFilter.Test.createStub(testName)
        val signature = TestsFilter.Signatures.TestSignature(
            name = className
        )
        Truth.assertThat(test.matched(setOf(signature))).isTrue()
    }

    @Test
    fun `when testName starts with signatureName and device name is different then test will not match signature`() {
        val className = "TestClass"
        val testName = "$className.method"
        val test = TestsFilter.Test.createStub(
            name = testName,
            deviceName = DeviceName("23")
        )
        val signature = TestsFilter.Signatures.TestSignature(
            name = className,
            deviceName = "22"
        )
        Truth.assertThat(test.matched(setOf(signature))).isFalse()
    }

    @Test
    fun `when include contains emptySet then all tests will be excluded`() {
        val result = IncludeByTestSignaturesFilter(
            source = TestsFilter.Signatures.Source.Code,
            signatures = emptySet()
        ).filter(
            TestsFilter.Test.createStub("TestClass.method")
        )
        Truth.assertThat(result)
            .isInstanceOf(TestsFilter.Result.Excluded.DoNotMatchIncludeSignature::class.java)
    }

    @Test
    fun `when test matched signature then it will be excluded`() {
        val result = IncludeByTestSignaturesFilter(
            source = TestsFilter.Signatures.Source.Code,
            signatures = setOf(
                TestsFilter.Signatures.TestSignature(
                    name = "TestClass.method"
                )
            )
        ).filter(
            TestsFilter.Test.createStub("TestClass.method")
        )
        Truth.assertThat(result).isInstanceOf(TestsFilter.Result.Included::class.java)
    }

    @Test
    fun `when test signature has deviceName then test will be matched using deviceName`() {
        val result = IncludeByTestSignaturesFilter(
            source = TestsFilter.Signatures.Source.Code,
            signatures = setOf(
                TestsFilter.Signatures.TestSignature(
                    name = "TestClass.method",
                    deviceName = "23"
                )
            )
        ).filter(
            TestsFilter.Test.createStub("TestClass.method", deviceName = DeviceName("22"))
        )
        Truth.assertThat(result)
            .isInstanceOf(TestsFilter.Result.Excluded.DoNotMatchIncludeSignature::class.java)
    }

    @Test
    fun `when test signature has deviceName then test will be matched using name`() {
        val result = IncludeByTestSignaturesFilter(
            source = TestsFilter.Signatures.Source.Code,
            signatures = setOf(
                TestsFilter.Signatures.TestSignature(
                    name = "TestClass.method"
                )
            )
        ).filter(
            TestsFilter.Test.createStub("TestClass.method", deviceName = DeviceName("22"))
        )
        Truth.assertThat(result).isInstanceOf(TestsFilter.Result.Included::class.java)
    }
}
