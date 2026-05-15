package com.avito.android.string_transform.internal.task.common

import com.avito.android.isFailure
import com.avito.android.string_transform.createKotlinModuleBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.parseKotlinModule
import com.avito.android.string_transform.sampleRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.metadata.jvm.UnstableMetadataApi

@OptIn(UnstableMetadataApi::class)
internal class KotlinModuleTransformerTest {

    private val transformer = KotlinModuleTransformer()

    @Test
    fun `kotlin module transformer - rewrites package fqn and facade names - when exact rule matches`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("module.kotlin_module").apply {
            writeBytes(createKotlinModuleBytes())
        }

        transformer.transform(inputFile, sampleRule()).getOrThrow()

        val module = parseKotlinModule(inputFile.readBytes())
        assertThat(module.packageParts.keys).containsExactly("com.example.changedvalue")
        val parts = module.packageParts.getValue("com.example.changedvalue")
        assertThat(parts.fileFacades).containsExactly("com/example/changedvalue/changedvalueKt")
        assertThat(parts.multiFileClassParts).containsExactly(
            "com/example/changedvalue/changedvalueMultifile__Part",
            "com/example/changedvalue/changedvalueMultifile",
        )
    }

    @Test
    fun `kotlin module transformer - tolerates length-changing replacement - when from and to differ in size`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("module.kotlin_module").apply {
            writeBytes(createKotlinModuleBytes(token = "originalvalue"))
        }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule(from = "originalvalue", to = "shortname")),
        ).getOrThrow()

        val module = parseKotlinModule(inputFile.readBytes())
        assertThat(module.packageParts.keys).containsExactly("com.example.shortname")
        val parts = module.packageParts.getValue("com.example.shortname")
        assertThat(parts.fileFacades).containsExactly("com/example/shortname/shortnameKt")
    }

    @Test
    fun `kotlin module transformer - leaves bytes untouched - when no rule matches`(
        @TempDir dir: File,
    ) {
        val originalBytes = createKotlinModuleBytes()
        val inputFile = dir.resolve("module.kotlin_module").apply {
            writeBytes(originalBytes)
        }

        transformer.transform(
            inputFile,
            listOf(NormalizedRule(from = "no-such-token", to = "irrelevant")),
        ).getOrThrow()

        assertThat(inputFile.readBytes()).isEqualTo(originalBytes)
    }

    @Test
    fun `kotlin module transformer - returns failure - when input bytes are not parseable`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("module.kotlin_module").apply {
            writeText("not-kotlin-module")
        }

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `kotlin module transformer - returns failure - when input file is missing`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("missing.kotlin_module")

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }
}
