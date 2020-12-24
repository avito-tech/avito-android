package com.avito.android.runner.annotation.validation

import com.avito.android.runner.annotation.resolver.TestKindExtractor
import com.avito.android.runner.annotation.resolver.TestMethodOrClass
import com.avito.report.model.Kind
import org.junit.rules.TestRule

class NoRulesWithSideEffectCheck : TestMetadataCheck {

    override fun validate(test: TestMethodOrClass) {
        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (TestKindExtractor.extract(test)) {
            Kind.UI_COMPONENT, Kind.UI_COMPONENT_STUB, Kind.E2E_STUB -> validateNoSideEffects(test.testClass)
        }
    }

    private fun validateNoSideEffects(testClass: Class<*>) {
        val rules = findTestRules(testClass)
        if (rules.isNotEmpty()) {
            throw IllegalStateException(
                "Test ${testClass.canonicalName} uses rules with side effects: ${rules.joinToString { it.simpleName }}. " +
                    "It makes test unstable. Replace these rules by hermetic equivalents or change type of test."
            )
        }
    }

    private fun findTestRules(testClass: Class<*>): Set<Class<TestRule>> {
        return testClass.declaredFields
            .filter {
                HasSideEffects::class.java.isAssignableFrom(it.type)
            }
            .map {
                @Suppress("UNCHECKED_CAST")
                it.type as Class<TestRule>
            }
            .toSet()
    }
}
