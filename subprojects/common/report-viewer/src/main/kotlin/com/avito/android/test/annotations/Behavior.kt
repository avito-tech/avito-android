package com.avito.android.test.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Behavior(val behavior: TestCaseBehavior)

enum class TestCaseBehavior(val tmsValue: Int) {
    POSITIVE(2), NEGATIVE(3), UNDEFINED(1);

    companion object {
        fun fromId(tmsId: Int): TestCaseBehavior? =
            values().find { it.tmsValue == tmsId }

        fun fromName(name: String): TestCaseBehavior? =
            values().find { it.name == name.toUpperCase() }
    }
}
