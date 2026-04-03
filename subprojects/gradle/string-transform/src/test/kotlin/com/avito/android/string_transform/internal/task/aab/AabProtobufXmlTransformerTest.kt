package com.avito.android.string_transform.internal.task.aab

import com.android.aapt.Resources
import com.avito.android.isFailure
import com.avito.android.string_transform.asTextFormat
import com.avito.android.string_transform.createProtobufXmlBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.sampleRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AabProtobufXmlTransformerTest {

    private val transformer = AabProtobufXmlTransformer()

    @Test
    fun `protobuf xml transformer - rewrites compiled xml text surfaces - when exact rule matches`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("compiled.xml").apply {
            writeBytes(createProtobufXmlBytes())
        }

        transformer.transform(inputFile, sampleRule()).getOrThrow()

        val xmlNode = Resources.XmlNode.parseFrom(inputFile.readBytes())
        val printed = xmlNode.asTextFormat()

        assertThat(printed).contains("changedvalue_node")
        assertThat(printed).contains("changedvalue_attr")
        assertThat(printed).contains("changedvalue")
        assertThat(printed).doesNotContain("samplevalue")
    }

    @Test
    fun `protobuf xml transformer - preserves protobuf structure - when rule matches field label only`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("compiled.xml").apply {
            writeBytes(createProtobufXmlBytes())
        }
        val original = Resources.XmlNode.parseFrom(inputFile.readBytes()).asTextFormat()

        transformer.transform(
            inputFile,
            listOf(NormalizedRule(from = "name", to = "renamed")),
        ).getOrThrow()

        val transformed = Resources.XmlNode.parseFrom(inputFile.readBytes()).asTextFormat()

        assertThat(transformed).isEqualTo(original)
    }

    @Test
    fun `protobuf xml transformer - returns failure - when compiled xml is corrupt`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("compiled.xml").apply {
            writeText("not-protobuf")
        }

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `protobuf xml transformer - returns failure - when input file is missing`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("missing.xml")

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }
}
