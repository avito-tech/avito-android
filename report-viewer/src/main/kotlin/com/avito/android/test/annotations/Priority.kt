package com.avito.android.test.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Priority(val priority: TestCasePriority)

enum class TestCasePriority(val tmsValue: Int) {
    CRITICAL(4), MAJOR(3), NORMAL(2), MINOR(1);

    companion object {
        fun fromId(tmsId: Int): TestCasePriority? = values().find { it.tmsValue == tmsId }

        fun fromName(name: String): TestCasePriority? = values().find { it.name == name.toUpperCase() }
    }
}
