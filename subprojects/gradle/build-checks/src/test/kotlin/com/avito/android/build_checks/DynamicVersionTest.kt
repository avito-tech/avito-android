package com.avito.android.build_checks

import com.avito.android.build_checks.internal.isDynamicVersion
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test

internal class DynamicVersionTest {

    @Test
    fun `non dynamic dependency versions`() {
        listOf(
            "1.0",
            "1.0-beta01",
            "1.0-SNAPSHOT" // changing version is a non-dynamic
        ).forEach { version ->
            assertWithMessage("Expected non-dynamic version $version")
                .that(isDynamicVersion(version)).isFalse()
        }
    }

    @Test
    fun `dynamic dependency versions`() {
        listOf(
            // Range
            "[1.0,)",
            "[1.1, 2.0)",
            "(1.2, 1.5]",
            "]1.0, 2.0[",
            // Prefix version range
            "+",
            "1.+",
            "1.3.+",
            // latest-status version
            "latest.release"
        ).forEach { version ->
            assertWithMessage("Expected dynamic version $version")
                .that(isDynamicVersion(version)).isTrue()
        }
    }
}
