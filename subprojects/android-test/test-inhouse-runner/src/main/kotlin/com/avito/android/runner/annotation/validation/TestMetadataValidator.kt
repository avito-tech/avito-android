package com.avito.android.runner.annotation.validation

import android.os.Bundle
import com.avito.android.runner.annotation.resolver.MethodStringRepresentation
import com.avito.android.runner.annotation.resolver.TestKindExtractor
import com.avito.android.runner.annotation.resolver.getClassOrThrow
import com.avito.report.model.Kind

internal class TestMetadataValidator {

    fun validate(instrumentationArguments: Bundle) {
        val test = instrumentationArguments.getString("class")

        if (test.isNullOrBlank()) {
            throw RuntimeException("Test name not found in instrumentation arguments: $instrumentationArguments")
        }
        val parsedTest = MethodStringRepresentation.parseString(test)

        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (TestKindExtractor.extract(parsedTest)) {
            Kind.UI_COMPONENT, Kind.UI_COMPONENT_STUB, Kind.E2E_STUB -> validateNoSideEffects(test)
        }
    }

    private fun validateNoSideEffects(test: String) {
        val testClass = MethodStringRepresentation.parseString(test).getClassOrThrow()

        val rules = NoSideEffectsRulesValidator.validate(testClass)
        if (rules.isNotEmpty()) {
            throw IllegalStateException(
                "Test ${testClass.canonicalName} uses rules with side effects: $rules. " +
                    "It makes test unstable. Replace these rules by hermetic equivalents or change type of test."
            )
        }
    }
}
