package com.avito.instrumentation.suite.dex.check

import com.avito.instrumentation.suite.dex.AnnotationData
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ExternalIdDuplicateCheckTest {

    private var detected = false

    private val onDuplicateDetected = { message: String ->
        println(message)
        detected = true
    }

    @Test
    fun `detector - detects nothing - for different ids`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        detector.onNewMethodFound(
            className = "com.avito.test.Test1",
            methodName = "test",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to "12345")
                )
            ),
            methodAnnotations = emptyList()
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "test",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to "54321")
                )
            ),
            methodAnnotations = emptyList()
        )

        assertThat(detected).isFalse()
    }

    @Test
    fun `detector - detects duplicate - for two different test classes with same externalId`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        val someId = UUID.randomUUID().toString()

        detector.onNewMethodFound(
            className = "com.avito.test.Test1",
            methodName = "test",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = emptyList()
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "test",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = emptyList()
        )

        assertThat(detected).isTrue()
    }

    @Test
    fun `detector - detects duplicate - for test class and different class method with same externalId`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        val someId = UUID.randomUUID().toString()

        detector.onNewMethodFound(
            className = "com.avito.test.Test1",
            methodName = "test",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = emptyList()
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "test",
            classAnnotations = emptyList(),
            methodAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            )
        )

        assertThat(detected).isTrue()
    }

    @Test
    fun `detector - detects duplicate - for two different test methods with same externalId`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        val someId = UUID.randomUUID().toString()

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "someTest1",
            classAnnotations = emptyList(),
            methodAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            )
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "someTest2",
            classAnnotations = emptyList(),
            methodAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            )
        )

        assertThat(detected).isTrue()
    }

    @Test
    fun `detector - detects duplicate - for two different test methods with class has externalId`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        val someId = UUID.randomUUID().toString()

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "someTest1",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = emptyList()
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "someTest2",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = emptyList()
        )

        assertThat(detected).isTrue()
    }

    @Test
    fun `detector - ignores duplicate - for two different test methods with dataSetNumbers and class has externalId`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        val someId = UUID.randomUUID().toString()

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "someTest1",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = listOf(
                AnnotationData(
                    DATA_SET_ANNOTATION_TYPE,
                    mapOf("value" to 1)
                )
            )
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test2",
            methodName = "someTest2",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = listOf(
                AnnotationData(
                    DATA_SET_ANNOTATION_TYPE,
                    mapOf("value" to 2)
                )
            )
        )

        assertThat(detected).isFalse()
    }

    @Test
    fun `detector - detects duplicate - for two different test methods one with dataSetNumber and class has externalId`() {

        val detector = ExternalIdDuplicateCheck(onDuplicateDetected)

        val someId = UUID.randomUUID().toString()

        detector.onNewMethodFound(
            className = "com.avito.test.Test",
            methodName = "someTest1",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = listOf(
                AnnotationData(
                    DATA_SET_ANNOTATION_TYPE,
                    mapOf("value" to 1)
                )
            )
        )

        detector.onNewMethodFound(
            className = "com.avito.test.Test",
            methodName = "someTest2",
            classAnnotations = listOf(
                AnnotationData(
                    EXTERNAL_ID_ANNOTATION_TYPE,
                    mapOf("value" to someId)
                )
            ),
            methodAnnotations = listOf()
        )

        assertThat(detected).isTrue()
    }
}
