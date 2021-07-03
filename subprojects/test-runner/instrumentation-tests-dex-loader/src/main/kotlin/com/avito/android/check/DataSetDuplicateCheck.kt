package com.avito.android.check

import com.avito.android.AnnotationData

public class DataSetDuplicateCheck(override val onViolation: (String) -> Unit) : TestSignatureCheck {

    private val dataSetNumberAnnotationValue = "value"

    private val classDataSets = mutableSetOf<Pair<TestLocation, Int>>()

    override fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    ) {
        val dataSetNumber: Int? = methodAnnotations
            .find { it.name.contains(DATA_SET_ANNOTATION_TYPE) }
            ?.getIntValue(dataSetNumberAnnotationValue)

        if (dataSetNumber != null) {
            val unique = classDataSets.add(className to dataSetNumber)

            if (!unique) {
                onViolation("$className contains data sets with duplicate number: $dataSetNumber")
            }
        }
    }
}
