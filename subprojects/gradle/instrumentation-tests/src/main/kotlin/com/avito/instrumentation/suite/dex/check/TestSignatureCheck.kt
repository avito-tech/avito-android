package com.avito.instrumentation.suite.dex.check

import com.avito.instrumentation.suite.dex.AnnotationData

interface TestSignatureCheck {

    val onViolation: (String) -> Unit

    fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    )
}
