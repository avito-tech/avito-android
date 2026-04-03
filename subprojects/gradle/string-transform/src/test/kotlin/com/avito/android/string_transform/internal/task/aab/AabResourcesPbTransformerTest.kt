package com.avito.android.string_transform.internal.task.aab

import com.android.aapt.Resources
import com.avito.android.isFailure
import com.avito.android.string_transform.asTextFormat
import com.avito.android.string_transform.createResourcesPbBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.sampleRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AabResourcesPbTransformerTest {

    private val transformer = AabResourcesPbTransformer()

    @Test
    fun `resources pb transformer - rewrites resource table text surfaces - when exact rule matches`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("resources.pb").apply {
            writeBytes(createResourcesPbBytes())
        }

        transformer.transform(inputFile, sampleRule()).getOrThrow()

        val resourceTable = Resources.ResourceTable.parseFrom(inputFile.readBytes())
        val printed = resourceTable.asTextFormat()

        assertThat(printed).contains("changedvalue_entry")
        assertThat(printed).contains("changedvalue_type")
        assertThat(printed).contains("com.example.changedvalue")
        assertThat(printed).contains("changedvalue")
        assertThat(printed).doesNotContain("samplevalue")
    }

    @Test
    fun `resources pb transformer - preserves protobuf structure - when rule matches field label only`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("resources.pb").apply {
            writeBytes(createResourcesPbBytes())
        }
        val original = Resources.ResourceTable.parseFrom(inputFile.readBytes()).asTextFormat()

        transformer.transform(
            inputFile,
            listOf(NormalizedRule(from = "package", to = "renamed")),
        ).getOrThrow()

        val transformed = Resources.ResourceTable.parseFrom(inputFile.readBytes()).asTextFormat()

        assertThat(transformed).isEqualTo(original)
    }

    @Test
    fun `resources pb transformer - returns failure - when resource table is corrupt`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("resources.pb").apply {
            writeText("not-protobuf")
        }

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `resources pb transformer - returns failure - when input file is missing`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("missing.pb")

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }
}
