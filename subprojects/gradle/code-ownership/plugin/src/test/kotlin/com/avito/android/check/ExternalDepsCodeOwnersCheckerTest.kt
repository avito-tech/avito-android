package com.avito.android.check

import com.avito.android.diff.provider.OwnersProvider
import com.avito.android.model.FakeOwners
import com.avito.android.model.FakeOwnersSerializer
import com.avito.android.utils.LIBS_OWNERS_TOML_CONTENT
import com.avito.android.utils.LIBS_VERSIONS_TOML_CONTENT
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ExternalDepsCodeOwnersCheckerTest {

    private val ownersProvider = OwnersProvider { FakeOwners.values().toSet() }
    private val ownersSerializer = FakeOwnersSerializer()
    private val checker = ExternalDepsCodeOwnersChecker(ownersSerializer, ownersProvider)

    @Test
    internal fun `check dependencies - only library section - success`(@TempDir projectDir: File) {
        checker.check(
            projectDir.createChildFile(
                name = "libs.versions.toml",
                content = """
                    [libraries]
                    androidx-constraintLayout = "Test Owner"
                """.trimIndent(),
            ),
            projectDir.createChildFile(
                name = "libs.owners.toml",
                content = """
                    [libraries]
                    androidx-constraintLayout = "Speed"  
                """.trimIndent()
            )
        )
    }

    @Test
    internal fun `check dependencies - valid content with different sections - success`(@TempDir projectDir: File) {
        checker.check(
            projectDir.createChildFile(
                name = "libs.versions.toml",
                content = LIBS_VERSIONS_TOML_CONTENT,
            ),
            projectDir.createChildFile(
                name = "libs.owners.toml",
                content = LIBS_OWNERS_TOML_CONTENT
            )
        )
    }

    @Test
    internal fun `check dependencies - dependency doesn't have an owner - error`(@TempDir projectDir: File) {
        val exception = assertThrows<RuntimeException> {
            checker.check(
                projectDir.createChildFile(
                    name = "libs.versions.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
                """.trimIndent(),
                ),
                projectDir.createChildFile(
                    name = "libs.owners.toml",
                    content = """
                    [libraries]
                """.trimIndent()
                )
            )
        }

        Truth
            .assertThat(exception.message)
            .contains("Dependency `androidx-constraintLayout` should have an owner, but it don't")
    }

    @Test
    internal fun `check dependencies - dependency has an invalid owner  - error`(@TempDir projectDir: File) {
        val exception = assertThrows<RuntimeException> {
            checker.check(
                projectDir.createChildFile(
                    name = "libs.versions.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
                """.trimIndent(),
                ),
                projectDir.createChildFile(
                    name = "libs.owners.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "Test Owner"
                """.trimIndent()
                )
            )
        }

        Truth
            .assertThat(exception.message)
            .contains("Dependency `androidx-constraintLayout` should have a valid owner, not `Test Owner`.")
    }

    @Test
    internal fun `check dependencies - dependency has an invalid owner  - valid owners printed`(
        @TempDir projectDir: File
    ) {
        val exception = assertThrows<RuntimeException> {
            checker.check(
                projectDir.createChildFile(
                    name = "libs.versions.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
                """.trimIndent(),
                ),
                projectDir.createChildFile(
                    name = "libs.owners.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "Test Owner"
                """.trimIndent()
                )
            )
        }

        val validOwners = FakeOwners.values().map(ownersSerializer::serialize)
        Truth
            .assertThat(exception.message)
            .contains("$validOwners")
    }

    @Test
    internal fun `check dependencies - unknown dependency has an owner - error`(@TempDir projectDir: File) {
        val exception = assertThrows<RuntimeException> {
            checker.check(
                projectDir.createChildFile(
                    name = "libs.versions.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.1"
                """.trimIndent(),
                ),
                projectDir.createChildFile(
                    name = "libs.owners.toml",
                    content = """
                    [libraries]
                    androidx-constraintLayout = "Speed"
                    androidx-unknownLibrary = "Speed"
                """.trimIndent()
                )
            )
        }
        Truth
            .assertThat(exception.message)
            .contains("Dependency `androidx-unknownLibrary` have an owner, but is not present")
    }

    companion object {
        fun File.createChildFile(name: String, content: String) =
            File(this, name).apply {
                createNewFile()
                writeText(content)
            }
    }
}
