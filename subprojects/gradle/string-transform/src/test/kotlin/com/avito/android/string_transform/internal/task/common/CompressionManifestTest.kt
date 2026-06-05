package com.avito.android.string_transform.internal.task.common

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.util.zip.ZipEntry

internal class CompressionManifestTest {

    @Test
    fun `methodFor - returns method - when path exists in manifest`() {
        val manifest = CompressionManifest(
            mapOf(
                "lib/arm64-v8a/libnative.so" to ZipEntry.STORED,
                "classes.dex" to ZipEntry.DEFLATED,
            )
        )

        assertThat(manifest.methodFor("lib/arm64-v8a/libnative.so")).isEqualTo(ZipEntry.STORED)
        assertThat(manifest.methodFor("classes.dex")).isEqualTo(ZipEntry.DEFLATED)
    }

    @Test
    fun `methodFor - returns null - when path is not in manifest`() {
        val manifest = CompressionManifest(
            mapOf("classes.dex" to ZipEntry.DEFLATED)
        )

        assertThat(manifest.methodFor("unknown/path.txt")).isNull()
    }

    @Test
    fun `remapKeys - remaps matching keys - preserving unmatched keys`() {
        val manifest = CompressionManifest(
            mapOf(
                "lib/arm64-v8a/libnative.so" to ZipEntry.STORED,
                "classes.dex" to ZipEntry.DEFLATED,
                "res/values/strings.xml" to ZipEntry.DEFLATED,
            )
        )

        val remapped = manifest.remapKeys(
            mapOf(
                "res/values/strings.xml" to "res/values/renamed.xml",
            )
        )

        assertThat(remapped.methodFor("res/values/renamed.xml")).isEqualTo(ZipEntry.DEFLATED)
        assertThat(remapped.methodFor("res/values/strings.xml")).isNull()
        assertThat(remapped.methodFor("lib/arm64-v8a/libnative.so")).isEqualTo(ZipEntry.STORED)
        assertThat(remapped.methodFor("classes.dex")).isEqualTo(ZipEntry.DEFLATED)
    }

    @Test
    fun `remapKeys - returns equivalent manifest - when mapping is empty`() {
        val manifest = CompressionManifest(
            mapOf(
                "a.txt" to ZipEntry.STORED,
                "b.txt" to ZipEntry.DEFLATED,
            )
        )

        val remapped = manifest.remapKeys(emptyMap())

        assertThat(remapped.methodFor("a.txt")).isEqualTo(ZipEntry.STORED)
        assertThat(remapped.methodFor("b.txt")).isEqualTo(ZipEntry.DEFLATED)
    }

    @Test
    fun `storedEntriesMissing - returns stored paths not in final set`() {
        val manifest = CompressionManifest(
            mapOf(
                "lib/arm64-v8a/libnative.so" to ZipEntry.STORED,
                "lib/x86/libnative.so" to ZipEntry.STORED,
                "classes.dex" to ZipEntry.DEFLATED,
                "resources.arsc" to ZipEntry.STORED,
            )
        )

        val missing = manifest.storedEntriesMissing(
            finalRelPaths = setOf("lib/arm64-v8a/libnative.so", "classes.dex", "resources.arsc")
        )

        assertThat(missing).containsExactly("lib/x86/libnative.so")
    }

    @Test
    fun `storedEntriesMissing - returns empty list - when all stored entries present`() {
        val manifest = CompressionManifest(
            mapOf(
                "lib/arm64-v8a/libnative.so" to ZipEntry.STORED,
                "classes.dex" to ZipEntry.DEFLATED,
            )
        )

        val missing = manifest.storedEntriesMissing(
            finalRelPaths = setOf("lib/arm64-v8a/libnative.so", "classes.dex")
        )

        assertThat(missing).isEmpty()
    }

    @Test
    fun `storedEntriesMissing - ignores deflated entries not in final set`() {
        val manifest = CompressionManifest(
            mapOf(
                "META-INF/CERT.RSA" to ZipEntry.DEFLATED,
                "classes.dex" to ZipEntry.DEFLATED,
            )
        )

        val missing = manifest.storedEntriesMissing(
            finalRelPaths = setOf("classes.dex")
        )

        assertThat(missing).isEmpty()
    }

    @Test
    fun `storedEntriesMissing - returns empty list - when manifest is empty`() {
        val manifest = CompressionManifest(emptyMap())

        val missing = manifest.storedEntriesMissing(
            finalRelPaths = setOf("classes.dex")
        )

        assertThat(missing).isEmpty()
    }
}
