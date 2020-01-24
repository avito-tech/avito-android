package com.test.fixtures

/**
 * Compile and load class in test runtime is doable, but seems like an overkill for such an easy check
 * Also Kotlin runtime compilation is an unstable API atm
 */
class ClassWithMethod {
    fun method() {}
}

@TestAnnotation1
@TestAnnotation2
@TestAnnotation3("class")
class ClassWithAnnotation {

    @TestAnnotation3("method")
    fun method() {
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class TestAnnotation1

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class TestAnnotation2

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class TestAnnotation3(
    val value: String
)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class TestAnnotation4
