package com.avito.android.contracts.platform.scheme.imports

import com.avito.android.contracts.platform.scheme.imports.data.SchemesImportService
import com.avito.android.contracts.platform.scheme.imports.data.models.ApiSchemeImportResponse
import com.google.common.truth.Truth.assertThat
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
        val service: SchemesImportService<*> = mock()
        val facade = ApiSchemesImportFacade(service)
        val expectedSchema = modelSchema
        val gatewey = "test_gateway"
        whenever(service.importScheme(gatewey, expectedSchema.path))
            .thenReturn(
                ApiSchemeImportResponse(
                    result = ApiSchemeImportResponse.Schema(
                        schema = mapOf(expectedSchema.path to expectedSchema.content)
                    )
                )
            )

        val result = facade.importSchemes(gatewey, expectedSchema.path, testDirectory)
        assertThat(result).hasSize(1)

        val generatedFile = result.first()
        assertTrue(generatedFile.path.endsWith(expectedSchema.path))
    }
}
