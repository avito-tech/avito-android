package com.avito.android.string_transform.internal.task

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class VariantApkInputResolverTest {

    private val resolver = VariantApkInputResolver()

    @Test
    fun `resolve single - returns apk candidate - when directory contains exactly one apk`(@TempDir dir: File) {
        val apk = dir.resolve("app-release.apk").apply {
            writeText("stub apk")
        }

        val resolved = resolver.resolveSingle(dir).getOrThrow()

        assertThat(resolved).isEqualTo(apk)
    }

    @Test
    fun `resolve single - fails with deterministic order - when directory contains multiple apks`(@TempDir dir: File) {
        dir.resolve("app-universal.apk").writeText("stub apk")
        dir.resolve("app-arm64.apk").writeText("stub apk")

        val error = assertThrows(IllegalArgumentException::class.java) {
            resolver.resolveSingle(dir).getOrThrow()
        }

        assertThat(error.message).isEqualTo(
            "Observed VariantApkOutputs do not contain exactly one publishable APK candidate: " +
                "[${dir.path}/app-arm64.apk, ${dir.path}/app-universal.apk]"
        )
    }
}
