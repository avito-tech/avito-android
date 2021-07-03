package com.avito.android.check

import com.avito.android.AnnotationData

public interface TestSignatureCheck {

    public val onViolation: (String) -> Unit

    public fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    )
}
