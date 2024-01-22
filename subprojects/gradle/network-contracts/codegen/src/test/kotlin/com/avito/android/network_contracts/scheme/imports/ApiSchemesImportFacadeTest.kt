package com.avito.android.network_contracts.scheme.imports

import com.avito.android.network_contracts.scheme.imports.data.ApiSchemesImportService
import com.avito.android.network_contracts.scheme.imports.data.models.ApiSchemeImportResponse
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ApiSchemesImportFacadeTest {

    @Test
    fun `success response from service - create schemes in the target directory`(
        @TempDir testDirectory: File
    ) = runTest {
        val service: ApiSchemesImportService = mock()
        val facade = ApiSchemesImportFacade(service)
        val expectedSchema = modelSchema

        whenever(service.importScheme(any()))
            .thenReturn(
                ApiSchemeImportResponse(
                    result = ApiSchemeImportResponse.Schema(
                        schema = mapOf(expectedSchema.path to expectedSchema.content)
                    )
                )
            )

        val result = facade.importSchemes(expectedSchema.path, testDirectory)
        assertThat(result).hasSize(1)

        val generatedFile = result.first()
        assertTrue(generatedFile.path.endsWith(expectedSchema.path))
    }
}
