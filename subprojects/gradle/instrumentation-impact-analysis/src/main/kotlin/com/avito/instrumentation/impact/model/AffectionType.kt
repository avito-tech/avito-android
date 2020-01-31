package com.avito.instrumentation.impact.model

internal enum class AffectionType {
    TEST_ADDED,
    TEST_COPIED,
    TEST_DELETED,
    TEST_MODIFIED,
    TEST_RENAMED,
    /**
     * Shows that test itself isn't changed, but changed classes in test source sets that are used byt test
     */
    DEPENDENT_TEST_CODE_CHANGED
}
