package com.avito.android.check

import com.avito.android.AnnotationData

interface TestSignatureCheck {

    val onViolation: (String) -> Unit

    fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    )
}
