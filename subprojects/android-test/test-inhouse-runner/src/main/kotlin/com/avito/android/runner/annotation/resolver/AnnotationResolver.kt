package com.avito.android.runner.annotation.resolver

/**
 * Extracts annotation value from test method or class
 *
 * see InHouseInstrumentationTestRunner.parseTestAnnotations
 */
open class AnnotationResolver<T : Annotation>(
    override val key: String,
    private val annotationClass: Class<T>,
    private val annotationResolutionGetter: (T) -> TestMetadataResolver.Resolution
) : TestMetadataResolver {

    override fun resolve(test: TestMethodOrClass): TestMetadataResolver.Resolution =
        if (test.testMethod == null) {
            test.testClass.resolveClassAnnotation(test)
        } else {
            test.testMethod.getAnnotation(annotationClass)
                ?.let { annotationResolutionGetter.invoke(it) }
                ?: test.testClass.resolveClassAnnotation(test)
        }

    private fun Class<*>.resolveClassAnnotation(test: TestMethodOrClass): TestMetadataResolver.Resolution =
        getAnnotation(annotationClass)
            ?.let { annotationResolutionGetter.invoke(it) }
            ?: TestMetadataResolver.Resolution.NothingToChange(defaultReason(test))

    private fun defaultReason(test: TestMethodOrClass): String =
        "$annotationClass annotation not specified for: $test"
}
