package com.avito.android.test.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Deprecated("Use Kind")
public annotation class Type(
    val type: TestCaseType
)

/**
 * Use Kind instead
 */
public enum class TestCaseType {
    E2E_UI,
    COMPONENT_UI,
    CHECKLIST,
    INTEGRATION,
    UNIT_TEST
}
