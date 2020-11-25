package com.avito.instrumentation.suite.dex.check

import com.avito.instrumentation.suite.dex.AnnotationData

class ClassAndMethodDuplicateAnnotationCheck(override val onViolation: (String) -> Unit) : TestSignatureCheck {

    override fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    ) {
        val duplicateAnnotations = classAnnotations.map { it.name }.intersect(methodAnnotations.map { it.name })
        if (duplicateAnnotations.isNotEmpty()) {
            onViolation(
                "method: $methodName and it's class: $className " +
                    "has duplicate annotations: $duplicateAnnotations"
            )
        }
    }
}
