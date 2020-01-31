package com.avito.android.test.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Deprecated("Use Kind")
annotation class Type(val type: TestCaseType)

@Deprecated("Use Kind")
enum class TestCaseType {
    E2E_UI,
    COMPONENT_UI,
    CHECKLIST,
    INTEGRATION,
    UNIT_TEST
}
