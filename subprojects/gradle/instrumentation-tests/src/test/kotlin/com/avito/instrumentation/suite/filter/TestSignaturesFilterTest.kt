package com.avito.instrumentation.suite.filter

import com.avito.instrumentation.createStub
import com.avito.instrumentation.internal.suite.filter.IncludeByTestSignaturesFilter
import com.avito.instrumentation.internal.suite.filter.TestsFilter
import com.avito.instrumentation.internal.suite.filter.matched
import com.avito.report.model.DeviceName
import com.avito.truth.isInstanceOf
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestSignaturesFilterTest {

    @Test
    fun `when testName equals signatureName then test will match signature`() {
        val testName = "TestClass.method"
        val test = TestsFilter.Test.createStub(testName)
        val signature = TestsFilter.Signatures.TestSignature(
            name = testName
        )
        assertThat(test.matched(setOf(signature))).isTrue()
    }

    @Test
    fun `when testName starts with signatureName then test will match signature`() {
        val className = "TestClass"
        val testName = "$className.method"
        val test = TestsFilter.Test.createStub(testName)
        val signature = TestsFilter.Signatures.TestSignature(
            name = className
        )
        assertThat(test.matched(setOf(signature))).isTrue()
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
        assertThat(test.matched(setOf(signature))).isFalse()
    }

    @Test
    fun `when include contains emptySet then all tests will be excluded`() {
        val result = IncludeByTestSignaturesFilter(
            source = TestsFilter.Signatures.Source.Code,
            signatures = emptySet()
        ).filter(
            TestsFilter.Test.createStub("TestClass.method")
        )
        assertThat(result).isInstanceOf<TestsFilter.Result.Excluded.DoesNotMatchIncludeSignature>()
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
        assertThat(result).isInstanceOf<TestsFilter.Result.Included>()
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
        assertThat(result).isInstanceOf<TestsFilter.Result.Excluded.DoesNotMatchIncludeSignature>()
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
        assertThat(result).isInstanceOf<TestsFilter.Result.Included>()
    }
}
