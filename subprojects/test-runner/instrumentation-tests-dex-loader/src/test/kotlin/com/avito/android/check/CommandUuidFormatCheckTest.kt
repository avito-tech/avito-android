package com.avito.android.check

import com.avito.android.AnnotationData
import com.avito.android.test.annotations.CommandUuid
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class CommandUuidFormatCheckTest {

    private val violations = mutableListOf<String>()

    private val onViolationDetected = { message: String ->
        violations += message
        Unit
    }

    @Test
    fun `annotation type - matches annotation class - to keep check working after rename`() {
        assertThat(COMMAND_UUID_ANNOTATION_TYPE).isEqualTo(CommandUuid::class.java.canonicalName)
    }

    @Test
    fun `check - detects nothing - for test without command uuid`() {
        check().onNewMethodFound(
            className = "Lcom/avito/test/Test;",
            methodName = "test",
            classAnnotations = emptyList(),
            methodAnnotations = emptyList()
        )

        assertThat(violations).isEmpty()
    }

    @Test
    fun `check - detects nothing - for other annotation with non uuid value`() {
        check().onNewMethodFound(
            className = "Lcom/avito/test/Test;",
            methodName = "test",
            classAnnotations = listOf(
                AnnotationData("com.avito.android.test.annotations.ExternalId", mapOf("value" to "12345"))
            ),
            methodAnnotations = emptyList()
        )

        assertThat(violations).isEmpty()
    }

    @Test
    fun `check - detects nothing - for valid command uuid on class`() {
        checkCommandUuid("b5a5ff6d-73ef-400e-abda-6a89b19e4729")

        assertThat(violations).isEmpty()
    }

    @Test
    fun `check - detects nothing - for uppercase command uuid`() {
        checkCommandUuid("B5A5FF6D-73EF-400E-ABDA-6A89B19E4729")

        assertThat(violations).isEmpty()
    }

    @Test
    fun `check - detects nothing - for command uuid surrounded by spaces`() {
        checkCommandUuid("  b5a5ff6d-73ef-400e-abda-6a89b19e4729  ")

        assertThat(violations).isEmpty()
    }

    @Test
    fun `check - detects violation - for team name instead of uuid`() {
        checkCommandUuid("mobile-core")

        assertThat(violations).hasSize(1)
        assertThat(violations.single()).contains("mobile-core")
        assertThat(violations.single()).contains("com.avito.test.Test.test")
    }

    @Test
    fun `check - detects violation - for uuid copied together with comment prefix`() {
        checkCommandUuid("commandUuid = b5a5ff6d-73ef-400e-abda-6a89b19e4729")

        assertThat(violations).hasSize(1)
    }

    @Test
    fun `check - detects violation - for uuid with missing character`() {
        checkCommandUuid("b5a5ff6d-73ef-400e-abda-6a89b19e472")

        assertThat(violations).hasSize(1)
    }

    @Test
    fun `check - detects nothing - for uuid with non breaking space`() {
        checkCommandUuid("b5a5ff6d-73ef-400e-abda-6a89b19e4729\u00A0")

        assertThat(violations).isEmpty()
    }

    @Test
    fun `check - detects violation - for uuid with zero width space`() {
        checkCommandUuid("b5a5ff6d-73ef-400e-abda-6a89b19e4729\u200B")

        assertThat(violations).hasSize(1)
        assertThat(violations.single()).contains("length")
    }

    @Test
    fun `check - detects violation - for empty command uuid`() {
        checkCommandUuid("")

        assertThat(violations).hasSize(1)
    }

    @Test
    fun `check - detects violation - for annotation without value`() {
        check().onNewMethodFound(
            className = "Lcom/avito/test/Test;",
            methodName = "test",
            classAnnotations = listOf(AnnotationData(COMMAND_UUID_ANNOTATION_TYPE, emptyMap())),
            methodAnnotations = emptyList()
        )

        assertThat(violations).hasSize(1)
        assertThat(violations.single()).contains("Can't read")
    }

    @Test
    fun `check - detects violation - for invalid command uuid on method`() {
        check().onNewMethodFound(
            className = "Lcom/avito/test/Test;",
            methodName = "test",
            classAnnotations = emptyList(),
            methodAnnotations = listOf(commandUuidAnnotation("mobile-core"))
        )

        assertThat(violations).hasSize(1)
    }

    private fun checkCommandUuid(value: String) {
        check().onNewMethodFound(
            className = "Lcom/avito/test/Test;",
            methodName = "test",
            classAnnotations = listOf(commandUuidAnnotation(value)),
            methodAnnotations = emptyList()
        )
    }

    private fun check() = CommandUuidFormatCheck(onViolationDetected)

    private fun commandUuidAnnotation(value: String) = AnnotationData(
        COMMAND_UUID_ANNOTATION_TYPE,
        mapOf("value" to value)
    )
}
