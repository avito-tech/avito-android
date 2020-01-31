package com.avito.android.test.report.model

enum class TestType {
    FUNCTIONAL,
    PERFORMANCE_FUNCTIONAL,
    PERFORMANCE_COMPONENT,
    SCREENSHOT,
    COMPONENT,
    PUBLISH,
    MESSENGER,
    MANUAL,
    UNIT,
    NONE;

    companion object {
        fun throwNoneType() {
            throw RuntimeException(
                "Invalid test type = TestType.None. Most likely, you forgot to specify test type by test class annotation" +
                    "(see test/annotations/TestType.kt for available options)"
            )
        }
    }
}
