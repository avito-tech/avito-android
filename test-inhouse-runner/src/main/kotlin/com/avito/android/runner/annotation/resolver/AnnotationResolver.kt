package com.avito.android.runner.annotation.resolver

/**
 * Used to extract annotation value from test method or class
 *
 * see InHouseInstrumentationTestRunner.parseTestAnnotations
 */
open class AnnotationResolver<T : Annotation>(
    override val key: String,
    private val annotationClass: Class<T>,
    private val annotationResolutionGetter: (T) -> TestMetadataResolver.Resolution
) : TestMetadataResolver {

    override fun resolve(test: String): TestMetadataResolver.Resolution =
        when (val parseResolution = MethodStringRepresentation.parseString(test)) {

            is MethodStringRepresentation.Resolution.ClassOnly -> {
                parseResolution.aClass.resolveClassAnnotation(test)
            }

            is MethodStringRepresentation.Resolution.Method ->
                parseResolution.method.getAnnotation(annotationClass)
                    ?.let { annotationResolutionGetter.invoke(it) }
                    ?: parseResolution.aClass.resolveClassAnnotation(test)

            is MethodStringRepresentation.Resolution.ParseError -> TestMetadataResolver.Resolution.NothingToChange(
                parseResolution.message
            )
        }

    private fun Class<*>.resolveClassAnnotation(test: String): TestMetadataResolver.Resolution =
        getAnnotation(annotationClass)
            ?.let { annotationResolutionGetter.invoke(it) }
            ?: TestMetadataResolver.Resolution.NothingToChange(defaultReason(test))

    private fun defaultReason(test: String): String =
        "$annotationClass annotation not specified for: $test"
}
