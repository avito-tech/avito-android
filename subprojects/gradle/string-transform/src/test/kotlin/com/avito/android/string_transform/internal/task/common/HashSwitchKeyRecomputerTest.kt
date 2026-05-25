package com.avito.android.string_transform.internal.task.common

import com.avito.android.isFailure
import com.avito.android.string_transform.createDexBytes
import com.avito.android.string_transform.createDexWithHashSwitchBytes
import com.avito.android.string_transform.createDexWithIfEqChainBytes
import com.avito.android.string_transform.hashSwitchCaseStrings
import com.avito.android.string_transform.hashSwitchKeys
import com.avito.android.string_transform.ifEqChainKeys
import com.avito.android.string_transform.internal.rules.NormalizedRule
import com.avito.android.string_transform.readDexFile
import com.avito.android.string_transform.sampleRule
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class HashSwitchKeyRecomputerTest {

    private val recomputer = HashSwitchKeyRecomputer()

    @Test
    fun `sparse hash-switch - keys rehashed to current case strings - when payload is stale`(@TempDir dir: File) {
        val finalCases = listOf("changedvalue_a", "changedvalue_b", "changedvalue_c")
        val staleKeys = listOf("samplevalue_a", "samplevalue_b", "samplevalue_c").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = finalCases, sparse = true, caseKeys = staleKeys))
        }
        assertThat(readDexFile(inputFile.readBytes(), dir).hashSwitchKeys())
            .isNotEqualTo(finalCases.map { it.hashCode() }.sorted())

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        val out = readDexFile(inputFile.readBytes(), dir)
        assertThat(warnings).isEmpty()
        assertThat(out.hashSwitchKeys()).isEqualTo(finalCases.map { it.hashCode() }.sorted())
        assertThat(out.hashSwitchCaseStrings()).containsExactlyElementsIn(finalCases)
    }

    @Test
    fun `sparse hash-switch - keys preserved - when case strings match keys`(@TempDir dir: File) {
        val cases = listOf("foo", "bar", "baz")
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = cases, sparse = true))
        }
        val before = readDexFile(inputFile.readBytes(), dir).hashSwitchKeys()

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        val after = readDexFile(inputFile.readBytes(), dir).hashSwitchKeys()
        assertThat(warnings).isEmpty()
        assertThat(after).isEqualTo(before)
        assertThat(after).isEqualTo(cases.map { it.hashCode() }.sorted())
    }

    @Test
    fun `packed hash-switch - keys rehashed - when rewrite preserves consecutiveness`(@TempDir dir: File) {
        // Single-character strings produce consecutive hashes (A=65, B=66, C=67 → X=88, Y=89, Z=90),
        // so the rewritten payload is still a valid packed-switch.
        val finalCases = listOf("X", "Y", "Z")
        val staleKeys = listOf("A", "B", "C").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = finalCases, sparse = false, caseKeys = staleKeys))
        }

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        val after = readDexFile(inputFile.readBytes(), dir).hashSwitchKeys()
        assertThat(warnings).isEmpty()
        assertThat(after).isEqualTo(finalCases.map { it.hashCode() }.sorted())
        assertThat(after.zipWithNext().all { (a, b) -> b - a == 1 }).isTrue()
    }

    @Test
    fun `packed hash-switch - warning emitted, payload preserved - when rewrite breaks consecutiveness`(
        @TempDir dir: File,
    ) {
        val finalCases = listOf("A", "longer_name", "C")
        val staleKeys = listOf("A", "B", "C").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = finalCases, sparse = false, caseKeys = staleKeys))
        }
        val before = readDexFile(inputFile.readBytes(), dir).hashSwitchKeys()

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        val after = readDexFile(inputFile.readBytes(), dir).hashSwitchKeys()
        assertThat(warnings).hasSize(1)
        assertThat(warnings.single().message).contains("packed-switch")
        assertThat(after).isEqualTo(before)
    }

    @Test
    fun `dex transformer - string pool and switch keys stay in sync - when rule rewrites case literals`(
        @TempDir dir: File,
    ) {
        val originalCases = listOf("samplevalue_alpha", "samplevalue_beta", "samplevalue_gamma")
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = originalCases, sparse = true))
        }

        val warnings = DexTransformer().transform(inputFile, sampleRule()).getOrThrow()

        val out = readDexFile(inputFile.readBytes(), dir)
        assertThat(warnings).isEmpty()
        val expectedCases = originalCases.map { it.replace("samplevalue", "changedvalue") }
        assertThat(out.hashSwitchCaseStrings()).containsExactlyElementsIn(expectedCases)
        assertThat(out.hashSwitchKeys()).isEqualTo(expectedCases.map { it.hashCode() }.sorted())
    }

    @Test
    fun `dex transformer - skips recompute pass - when rules are empty`(@TempDir dir: File) {
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = listOf("foo", "bar")))
        }
        val before = inputFile.readBytes()

        val warnings = DexTransformer().transform(inputFile, rules = emptyList()).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(inputFile.readBytes()).isEqualTo(before)
    }

    @Test
    fun `recomputer - returns failure - when input is not a valid dex`(@TempDir dir: File) {
        val inputFile = dir.resolve("classes.dex").apply { writeBytes(byteArrayOf(0, 1, 2, 3)) }

        val result = recomputer.recompute(inputFile)

        assertThat(result.isFailure()).isTrue()
    }

    @Test
    fun `recomputer - leaves dex unchanged - when no method contains hash-switch pattern`(@TempDir dir: File) {
        val inputFile = dir.resolve("classes.dex").apply { writeBytes(createDexBytes()) }
        val before = inputFile.readBytes()

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(inputFile.readBytes()).isEqualTo(before)
    }

    @Test
    fun `sparse hash-switch - mixed cases - rehashes brand keys and leaves non-brand keys intact`(@TempDir dir: File) {
        val finalCases = listOf("foo", "changedvalue_x", "bar")
        val staleKeys = listOf("foo", "samplevalue_x", "bar").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = finalCases, sparse = true, caseKeys = staleKeys))
        }

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        val after = readDexFile(inputFile.readBytes(), dir).hashSwitchKeys()
        assertThat(warnings).isEmpty()
        assertThat(after).isEqualTo(finalCases.map { it.hashCode() }.sorted())
    }

    @Test
    fun `if-eq chain - const keys rehashed to current case strings - when chain is stale`(@TempDir dir: File) {
        val finalCases = listOf("changedvalue_a", "changedvalue_b")
        val staleKeys = listOf("samplevalue_a", "samplevalue_b").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithIfEqChainBytes(cases = finalCases, caseKeys = staleKeys))
        }

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(readDexFile(inputFile.readBytes(), dir).ifEqChainKeys())
            .containsExactlyElementsIn(finalCases.map { it.hashCode() })
    }

    @Test
    fun `if-eq chain - brand case after narrow const is still rehashed`(@TempDir dir: File) {
        // "K" (hash 75) loads via const/16 and precedes the brand case. The walk must not
        // terminate at the narrow const, otherwise the trailing brand key is missed.
        val finalCases = listOf("K", "changedvalue_brand")
        val staleKeys = listOf("K".hashCode(), "samplevalue_brand".hashCode())
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithIfEqChainBytes(cases = finalCases, caseKeys = staleKeys))
        }

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(readDexFile(inputFile.readBytes(), dir).ifEqChainKeys())
            .containsExactly("K".hashCode(), "changedvalue_brand".hashCode())
    }

    @Test
    fun `if-eq chain - inverted last case via if-ne is rehashed`(@TempDir dir: File) {
        // R8 emits the final case as `if-ne hash, key, default` with the body as fall-through.
        val finalCases = listOf("changedvalue_a", "changedvalue_b")
        val staleKeys = listOf("samplevalue_a", "samplevalue_b").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithIfEqChainBytes(cases = finalCases, caseKeys = staleKeys, invertLastCase = true))
        }

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(readDexFile(inputFile.readBytes(), dir).ifEqChainKeys())
            .containsExactlyElementsIn(finalCases.map { it.hashCode() })
    }

    @Test
    fun `if-eq chain - narrow const key untouched - when case string unchanged`(@TempDir dir: File) {
        val cases = listOf("K", "M")
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithIfEqChainBytes(cases = cases))
        }
        val before = readDexFile(inputFile.readBytes(), dir).ifEqChainKeys()

        val warnings = recomputer.recompute(inputFile).getOrThrow()

        assertThat(warnings).isEmpty()
        assertThat(readDexFile(inputFile.readBytes(), dir).ifEqChainKeys()).isEqualTo(before)
    }

    @Test
    fun `dex transformer - aggregates packed-switch warnings into report`(@TempDir dir: File) {
        val finalCases = listOf("A", "B", "changedvalue_long_replacement")
        val staleKeys = listOf("A", "B", "C").map { it.hashCode() }
        val inputFile = dir.resolve("classes.dex").apply {
            writeBytes(createDexWithHashSwitchBytes(cases = finalCases, sparse = false, caseKeys = staleKeys))
        }
        // A rule that doesn't match anything keeps the first pass identity; the recompute
        // pass still sees the stale packed keys and surfaces the warning.
        val unrelatedRule = listOf(NormalizedRule(from = "this-substring-doesn-t-occur", to = "noop"))

        val warnings = DexTransformer().transform(inputFile, unrelatedRule).getOrThrow()

        assertThat(warnings).isNotEmpty()
        assertThat(warnings.any { it.message.contains("packed-switch") }).isTrue()
    }
}
