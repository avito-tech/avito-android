package com.avito.android.runner.annotation.resolver

internal object TestAnnotationExtractor {

    fun extract(test: String, vararg subset: Class<out Annotation>): Set<Annotation> {
        return when (val methodResolution = MethodStringRepresentation.parseString(test)) {

            is MethodStringRepresentation.Resolution.ClassOnly -> Annotations.getAnnotationsSubset(
                methodResolution.aClass,
                null,
                *subset
            )
            is MethodStringRepresentation.Resolution.Method -> Annotations.getAnnotationsSubset(
                methodResolution.aClass,
                methodResolution.method,
                *subset
            )
            is MethodStringRepresentation.Resolution.ParseError ->
                throw RuntimeException("Failed to parse annotations from $test")
        }
    }
}
