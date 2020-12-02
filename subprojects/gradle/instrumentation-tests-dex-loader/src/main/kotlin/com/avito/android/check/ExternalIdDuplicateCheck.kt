package com.avito.android.check

import com.avito.android.AnnotationData

internal typealias ExternalId = String
internal typealias TestLocation = String

class ExternalIdDuplicateCheck(override val onViolation: (String) -> Unit) : TestSignatureCheck {

    private val externalIdAnnotationValue = "value"
    private val dataSetNumberAnnotationValue = "value"

    private data class TestValues(val location: TestLocation, val dataSetNumber: Int?)

    private val externalIdValues = mutableMapOf<ExternalId, TestValues>()

    override fun onNewMethodFound(
        className: String,
        methodName: String,
        classAnnotations: List<AnnotationData>,
        methodAnnotations: List<AnnotationData>
    ) {
        val externalId: ExternalId? =
            methodAnnotations
                .find { it.name.contains(EXTERNAL_ID_ANNOTATION_TYPE) }
                ?.getStringValue(externalIdAnnotationValue)
                ?: classAnnotations
                    .find { it.name.contains(EXTERNAL_ID_ANNOTATION_TYPE) }
                    ?.getStringValue(externalIdAnnotationValue)

        val dataSetNumber: Int? = methodAnnotations
            .find { it.name.contains(DATA_SET_ANNOTATION_TYPE) }
            ?.getIntValue(dataSetNumberAnnotationValue)

        if (externalId != null) {
            addExternalId(externalId, "$className.$methodName", dataSetNumber)
        }
    }

    private fun addExternalId(externalId: ExternalId, location: TestLocation, dataSetNumber: Int?) {
        val testValues: TestValues? = externalIdValues.put(externalId, TestValues(location, dataSetNumber))

        if (testValues?.location != null && dataSetNumber == null) {
            onViolation("Duplicate @ExternalId found: $externalId in: ${testValues.location} and $location")
        }
    }
}

// visible in test
const val EXTERNAL_ID_ANNOTATION_TYPE = "com.avito.android.test.annotations.ExternalId"
const val DATA_SET_ANNOTATION_TYPE = "com.avito.android.test.annotations.DataSetNumber"
