package com.avito.android.string_transform.internal.task.aab

import com.avito.android.isFailure
import com.avito.android.string_transform.createDexBytes
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.internal.task.apk.OperationWarning
import com.avito.android.string_transform.primaryClassSourceFile
import com.avito.android.string_transform.primaryClassType
import com.avito.android.string_transform.primaryFieldInitialStringValue
import com.avito.android.string_transform.primaryFieldName
import com.avito.android.string_transform.primaryMethodConstString
import com.avito.android.string_transform.primaryMethodName
import com.avito.android.string_transform.primaryMethodParameterName
import com.avito.android.string_transform.readDexFile
import com.avito.android.string_transform.sampleRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AabDexTransformerTest {

    private val transformer = AabDexTransformer()

    @Test
    fun `dex transformer - rewrites class and string surfaces - when exact rule matches`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexBytes())
        }

        val warnings = transformer.transform(inputFile, sampleRule()).getOrThrow()

        val dexFile = readDexFile(inputFile.readBytes(), dir)

        assertThat(warnings).isEmpty()
        assertThat(dexFile.primaryClassType()).isEqualTo("Lcom/example/changedvalue/Holder;")
        assertThat(dexFile.primaryClassSourceFile()).isEqualTo("changedvalue.kt")
        assertThat(dexFile.primaryFieldName()).isEqualTo("changedvalueField")
        assertThat(dexFile.primaryFieldInitialStringValue()).isEqualTo("changedvalue")
        assertThat(dexFile.primaryMethodName()).isEqualTo("changedvalueMethod")
        assertThat(dexFile.primaryMethodParameterName()).isEqualTo("changedvalueParam")
        assertThat(dexFile.primaryMethodConstString()).isEqualTo("changedvalue")
    }

    @Test
    fun `dex transformer - keeps file unchanged - when rules are empty`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexBytes())
        }
        val originalBytes = inputFile.readBytes()

        val warnings = transformer.transform(inputFile, emptyList()).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(inputFile.readBytes()).isEqualTo(originalBytes)
    }

    @Test
    fun `dex transformer - rewrites literals sequentially - when multiple exact rules are declared`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexBytes())
        }

        val warnings = transformer.transform(
            inputFile,
            listOf(
                NormalizedRule(from = "samplevalue", to = "midvalue"),
                NormalizedRule(from = "midvalue", to = "changedvalue"),
            ),
        ).getOrThrow()

        val dexFile = readDexFile(inputFile.readBytes(), dir)
        assertThat(warnings).isEmpty()
        assertThat(dexFile.primaryClassType()).isEqualTo("Lcom/example/changedvalue/Holder;")
        assertThat(dexFile.primaryClassSourceFile()).isEqualTo("changedvalue.kt")
        assertThat(dexFile.primaryFieldName()).isEqualTo("changedvalueField")
        assertThat(dexFile.primaryFieldInitialStringValue()).isEqualTo("changedvalue")
        assertThat(dexFile.primaryMethodName()).isEqualTo("changedvalueMethod")
        assertThat(dexFile.primaryMethodParameterName()).isEqualTo("changedvalueParam")
        assertThat(dexFile.primaryMethodConstString()).isEqualTo("changedvalue")
    }

    @Test
    fun `dex transformer - returns failure - when dex file is corrupt`(
        @TempDir dir: File,
    ) {
        val inputFile = dir.resolve("classes.dex").apply {
            writeText("not-a-dex")
        }

        val result = transformer.transform(inputFile, sampleRule())

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `dex pending literal warnings - reports surviving literals - when output still contains original token`() {
        val warnings = dexPendingLiteralWarnings(
            outputBytes = "prefix samplevalue suffix".toByteArray(),
            rules = sampleRule(),
            affectedPath = "base/dex/classes.dex",
        )

        assertThat(warnings).containsExactly(
            OperationWarning(
                message = "DEX transform completed, but literal remained after semantic rewrite: 'samplevalue'",
                affectedPath = "base/dex/classes.dex",
            ),
        )
    }

    @Test
    fun `dex pending literal warnings - stays empty - when output no longer contains original token`() {
        val warnings = dexPendingLiteralWarnings(
            outputBytes = "prefix changedvalue suffix".toByteArray(),
            rules = sampleRule(),
            affectedPath = "base/dex/classes.dex",
        )

        assertThat(warnings).isEmpty()
    }
}
