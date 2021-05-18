package com.avito.android.check

import com.avito.android.AnnotationData
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class DataSetDuplicateCheckTest {

    private var detected = false

    private val onDuplicateDetected = { message: String ->
        println(message)
        detected = true
    }

    @Test
    fun `duplicate detected - if two methods in class has same dataSetNumber`() {
        DataSetDuplicateCheck(onDuplicateDetected).run {
            onNewMethodFound(
                "com.avito.Test",
                "dataSet1",
                classAnnotations = emptyList(),
                methodAnnotations = listOf(
                    AnnotationData(
                        DATA_SET_ANNOTATION_TYPE,
                        mapOf("value" to 44)
                    )
                )
            )

            onNewMethodFound(
                "com.avito.Test",
                "dataSet2",
                classAnnotations = emptyList(),
                methodAnnotations = listOf(
                    AnnotationData(
                        DATA_SET_ANNOTATION_TYPE,
                        mapOf("value" to 44)
                    )
                )
            )
        }

        assertThat(detected).isTrue()
    }

    @Test
    fun `duplicate not detected - if two methods in class has different dataSetNumber`() {
        DataSetDuplicateCheck(onDuplicateDetected).run {
            onNewMethodFound(
                "com.avito.Test",
                "dataSet1",
                classAnnotations = emptyList(),
                methodAnnotations = listOf(
                    AnnotationData(
                        DATA_SET_ANNOTATION_TYPE,
                        mapOf("value" to 12)
                    )
                )
            )

            onNewMethodFound(
                "com.avito.Test",
                "dataSet2",
                classAnnotations = emptyList(),
                methodAnnotations = listOf(
                    AnnotationData(
                        DATA_SET_ANNOTATION_TYPE,
                        mapOf("value" to 14)
                    )
                )
            )
        }

        assertThat(detected).isFalse()
    }

    @Test
    fun `duplicate not detected - if two methods from different classes has same dataSetNumber`() {
        DataSetDuplicateCheck(onDuplicateDetected).run {
            onNewMethodFound(
                "com.avito.Test1",
                "dataSet1",
                classAnnotations = emptyList(),
                methodAnnotations = listOf(
                    AnnotationData(DATA_SET_ANNOTATION_TYPE, mapOf("value" to 5))
                )
            )

            onNewMethodFound(
                "com.avito.Test2",
                "dataSet1",
                classAnnotations = emptyList(),
                methodAnnotations = listOf(AnnotationData(DATA_SET_ANNOTATION_TYPE, mapOf("value" to 5)))
            )
        }

        assertThat(detected).isFalse()
    }
}
