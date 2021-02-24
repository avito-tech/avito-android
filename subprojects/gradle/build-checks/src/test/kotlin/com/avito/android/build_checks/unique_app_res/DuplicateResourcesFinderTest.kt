package com.avito.android.build_checks.unique_app_res

import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import com.avito.android.build_checks.internal.unique_app_res.DuplicateResourcesFinder.ResourceDuplicate
import com.avito.android.build_checks.internal.unique_app_res.DuplicateResourcesFinderImpl
import com.avito.android.build_checks.internal.unique_app_res.Resource
import com.avito.android.build_checks.internal.unique_app_res.parsePackageAwareR
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class DuplicateResourcesFinderTest {

    private lateinit var packageAwareRFile: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        packageAwareRFile = File(tempDir.toFile(), "package-aware-r.txt")
    }

    @Test
    fun `empty result - no duplicates`() {
        val symbolsA = readSymbols(
            """
            |lib.a
            |id button
            |layout details
            |string auth
        """.trimMargin()
        )

        val symbolsB = readSymbols(
            """
            |lib.b
            |id hint
            |layout main
            |string title
        """.trimMargin()
        )

        val duplicates = findDuplicates(listOf(symbolsA, symbolsB))

        assertThat(duplicates).isEmpty()
    }

    @Test
    fun `empty result - ignored resource type`() {
        val symbolsA = readSymbols(
            """
            |lib.a
            |string title
        """.trimMargin()
        )

        val symbolsB = readSymbols(
            """
            |lib.b
            |string title
        """.trimMargin()
        )

        val duplicates = findDuplicates(
            listOf(symbolsA, symbolsB),
            ignoredTypes = setOf(ResourceType.STRING)
        )

        assertThat(duplicates).isEmpty()
    }

    @Test
    fun `empty result - ignored specific resource`() {
        val symbolsA = readSymbols(
            """
            |lib.a
            |string title
        """.trimMargin()
        )

        val symbolsB = readSymbols(
            """
            |lib.b
            |string title
        """.trimMargin()
        )

        val duplicates = findDuplicates(
            listOf(symbolsA, symbolsB),
            ignoredResources = setOf(
                Resource(ResourceType.STRING, "title")
            )
        )

        assertThat(duplicates).isEmpty()
    }

    @Test
    fun `duplicates - the same layout name`() {
        val symbolsA = readSymbols(
            """
            |lib.a
            |id button
            |layout details
            |string auth
        """.trimMargin()
        )

        val symbolsB = readSymbols(
            """
            |lib.b
            |id hint
            |layout details
            |string title
        """.trimMargin()
        )

        val duplicates = findDuplicates(listOf(symbolsA, symbolsB))

        assertThat(duplicates).hasSize(1)
        val duplicate = duplicates.first()
        assertThat(duplicate.resource.name).isEqualTo("details")
        assertThat(duplicate.resource.type).isInstanceOf(ResourceType.LAYOUT::class.java)
        assertThat(duplicate.packages).isEqualTo(listOf("lib.a", "lib.b"))
    }

    private fun findDuplicates(
        symbols: List<SymbolTable>,
        ignoredTypes: Set<ResourceType> = emptySet(),
        ignoredResources: Set<Resource> = emptySet()
    ): List<ResourceDuplicate> {
        return DuplicateResourcesFinderImpl(
            symbols,
            ignoredTypes,
            ignoredResources
        ).findDuplicates()
    }

    private fun readSymbols(content: String): SymbolTable {
        packageAwareRFile.writeText(content)
        return parsePackageAwareR(packageAwareRFile.toPath())
    }
}
