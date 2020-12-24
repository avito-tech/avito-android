package com.avito.android.runner.annotation.resolver

internal object TestAnnotationExtractor {

    fun extract(test: MethodStringRepresentation.Resolution, vararg subset: Class<out Annotation>): Set<Annotation> {
        return when (test) {

            is MethodStringRepresentation.Resolution.ClassOnly -> Annotations.getAnnotationsSubset(
                test.aClass,
                null,
                *subset
            )
            is MethodStringRepresentation.Resolution.Method -> Annotations.getAnnotationsSubset(
                test.aClass,
                test.method,
                *subset
            )
            is MethodStringRepresentation.Resolution.ParseError ->
                throw RuntimeException("Failed to parse annotations from $test")
        }
    }
}
