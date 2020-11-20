package com.avito.android.runner.annotation.resolver

import com.avito.android.runner.annotation.resolver.MethodStringRepresentation.Resolution
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation.Resolution.ClassOnly
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation.Resolution.Method
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation.Resolution.ParseError
import com.test.fixtures.ClassWithMethod
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class MethodStringRepresentationTest {

    @Test
    fun `parseString - resolution error - if empty string`() {
        assertResolution(
            "",
            ParseError("Method string representation is empty string")
        )
    }

    @Test
    fun `parseString - resolution class - for class only string`() {
        assertResolution(
            "com.test.fixtures.ClassWithMethod",
            ClassOnly(ClassWithMethod::class.java)
        )
    }

    @Test
    fun `parseString - resolution method - for class#method string`() {
        assertResolution(
            "com.test.fixtures.ClassWithMethod#method",
            Method(
                ClassWithMethod::class.java,
                ClassWithMethod::class.java.getMethod("method")
            )
        )
    }

    @Test
    fun `parseString - resolution class not found - for nonexistent class`() {
        val stringRepresentation = "com.test.fixtures.NonExistentClass"
        assertResolution(
            stringRepresentation,
            ParseError("Can't find class $stringRepresentation")
        )
    }

    @Test
    fun `parseString - resolution class not found - for nonexistent method`() {
        val stringRepresentation = "com.test.fixtures.ClassWithMethod#nonExistentMethod"
        assertResolution(
            stringRepresentation,
            ParseError("Can't find method nonExistentMethod in class com.test.fixtures.ClassWithMethod")
        )
    }

    private fun assertResolution(methodStringRepresentation: String, resolution: Resolution) {
        assertThat(
            MethodStringRepresentation.parseString(methodStringRepresentation),
            equalTo<Resolution>(resolution)
        )
    }
}
