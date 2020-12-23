package com.avito.android.runner.annotation.validation

import org.junit.rules.TestRule

internal object NoSideEffectsRulesValidator {

    fun validate(testClass: Class<*>): Set<Class<TestRule>> {
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
