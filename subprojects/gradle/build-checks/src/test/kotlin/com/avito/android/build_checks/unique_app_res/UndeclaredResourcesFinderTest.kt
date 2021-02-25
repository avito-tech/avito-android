package com.avito.android.build_checks.unique_app_res

import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import com.avito.android.build_checks.internal.unique_app_res.Resource
import com.avito.android.build_checks.internal.unique_app_res.UndeclaredResourcesFinderImpl
import com.avito.android.build_checks.internal.unique_app_res.parsePackageAwareR
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class UndeclaredResourcesFinderTest {

    private lateinit var packageAwareRFile: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        packageAwareRFile = File(tempDir.toFile(), "package-aware-r.txt")
    }

    @Test
    fun `empty result - all ignored resources found`() {
        val symbolsA = readSymbols(
            """
            |lib.a
            |layout details
            |string auth
        """.trimMargin()
        )

        val symbolsB = readSymbols(
            """
            |lib.b
            |layout main
            |string title
        """.trimMargin()
        )

        val duplicates = findResources(
            listOf(symbolsA, symbolsB),
            resources = setOf(
                Resource(ResourceType.STRING, "title"),
                Resource(ResourceType.LAYOUT, "details"),
            )
        )

        assertThat(duplicates).isEmpty()
    }

    @Test
    fun `found undeclared - ignored resource is not in apps`() {
        val symbols = readSymbols(
            """
            |lib.a
            |string title
            |layout main
        """.trimMargin()
        )

        val unknownRes = Resource(ResourceType.STRING, "unknown_res")
        val duplicates = findResources(
            listOf(symbols),
            resources = setOf(unknownRes)
        )

        assertThat(duplicates).contains(unknownRes)
    }

    private fun findResources(
        symbols: List<SymbolTable>,
        resources: Set<Resource> = emptySet()
    ): Set<Resource> {
        return UndeclaredResourcesFinderImpl(
            symbols,
            resources
        ).findResources()
    }

    private fun readSymbols(content: String): SymbolTable {
        packageAwareRFile.writeText(content)
        return parsePackageAwareR(packageAwareRFile.toPath())
    }
}
