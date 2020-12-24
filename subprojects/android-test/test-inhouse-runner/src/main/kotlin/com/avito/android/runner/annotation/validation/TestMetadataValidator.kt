package com.avito.android.runner.annotation.validation

import android.os.Bundle
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation
import com.avito.android.runner.annotation.resolver.getTestOrThrow

interface TestMetadataValidator {

    fun validate(instrumentationArguments: Bundle) // TODO: don't use Bundle
}

class TestMetadataValidatorImpl(
    private val checks: List<TestMetadataCheck>
) : TestMetadataValidator {

    override fun validate(instrumentationArguments: Bundle) {
        val test = instrumentationArguments.getString("class")

        if (test.isNullOrBlank()) {
            throw RuntimeException("Test name not found in instrumentation arguments: $instrumentationArguments")
        }
        val testMethod = MethodStringRepresentation.parseString(test).getTestOrThrow()

        checks.forEach {
            it.validate(testMethod)
        }
    }
}
