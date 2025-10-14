package com.avito.android.contracts.network.scheme.fixation.upsert.data

import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.fixation.service.data.ApiSchemesMapper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
class ApiSchemesMapperTest {

    @Test
    fun `when receive not empty schemes - return non empty valid requests`() {
        val author = "test"
        val version = "develop"
        val projectName = "test_project"
        val schemes = listOf(
            "api1" to "content1",
            "api2" to "content2"
        )

        val schemesMetadata = listOf(
            ApiSchemesMetadata(projectName, schemes.toMap())
        )

        val requests = ApiSchemesMapper.mapSchemesToRequest(schemesMetadata) { schemesProjectName, innerSchemes ->
            TestSchemesRequest(author, schemesProjectName, version, innerSchemes)
        }
        assertThat(requests).hasSize(schemesMetadata.size)

        val expectedRequest = TestSchemesRequest(
            author,
            projectName,
            version,
            schemes
        )
        assertThat(requests).contains(expectedRequest)
    }

    @Test
    fun `when receive not empty schemes with same project name - return non empty valid requests associated with project`() {
        val author = "test"
        val version = "develop"
        val projectName = "test_project"
        val schemes1 = "api1" to "content1"
        val schemes2 = "api2" to "content2"

        val schemesMetadata = listOf(
            ApiSchemesMetadata(projectName, mapOf(schemes1)),
            ApiSchemesMetadata(projectName, mapOf(schemes2)),
        )

        val expectedSchemes = listOf(schemes1, schemes2)

        val requests = ApiSchemesMapper.mapSchemesToRequest(schemesMetadata) { schemesProjectName, innerSchemes ->
            TestSchemesRequest(author, schemesProjectName, version, innerSchemes)
        }
        assertThat(requests).hasSize(1)

        val expectedRequest = TestSchemesRequest(
            author,
            projectName,
            version,
            expectedSchemes
        )
        assertThat(requests).contains(expectedRequest)
    }

    @Test
    fun `when receive empty schemes for project - return empty requests`() {
        val author = "test"
        val version = "develop"
        val projectName = "test_project"

        val schemesMetadata = listOf(
            ApiSchemesMetadata(projectName, emptyMap()),
        )

        val requests = ApiSchemesMapper.mapSchemesToRequest(schemesMetadata) { schemesProjectName, innerSchemes ->
            TestSchemesRequest(author, schemesProjectName, version, innerSchemes)
        }
        assertThat(requests).isEmpty()
    }

    private data class TestSchemesRequest(
        val author: String,
        val projectName: String,
        val version: String,
        val schemes: List<Pair<String, String>>,
    )
}
