package com.avito.android.network_contracts.scheme.fixation.upsert.data

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.scheme.fixation.upsert.data.models.UpdateApiSchemesRequest
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength")
class ApiSchemesMapperTest {

    @Test
    fun `when receive not empty schemes - return non empty valid requests`() {
        val author = "test"
        val version = "develop"
        val projectName = "test_project"
        val schemes = mapOf(
            "api1" to "content1",
            "api2" to "content2"
        )

        val schemesMetadata = listOf(
            ApiSchemesMetadata(projectName, schemes)
        )

        val requests = ApiSchemesMapper.mapSchemesToRequest(author, version, schemesMetadata)
        assertThat(requests).hasSize(schemesMetadata.size)

        val expectedRequest = UpdateApiSchemesRequest(
            author,
            projectName,
            version,
            UpdateApiSchemesRequest.Schema(schemes)
        )
        assertThat(requests).contains(expectedRequest)
    }

    @Test
    fun `when receive not empty schemes with same project name - return non empty valid requests associated with project`() {
        val author = "test"
        val version = "develop"
        val projectName = "test_project"
        val schemes1 = mapOf("api1" to "content1")
        val schemes2 = mapOf("api2" to "content2")

        val schemesMetadata = listOf(
            ApiSchemesMetadata(projectName, schemes1),
            ApiSchemesMetadata(projectName, schemes2),
        )

        val expectedSchemes = schemes1 + schemes2

        val requests = ApiSchemesMapper.mapSchemesToRequest(author, version, schemesMetadata)
        assertThat(requests).hasSize(1)

        val expectedRequest = UpdateApiSchemesRequest(
            author,
            projectName,
            version,
            UpdateApiSchemesRequest.Schema(expectedSchemes)
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

        val requests = ApiSchemesMapper.mapSchemesToRequest(author, version, schemesMetadata)
        assertThat(requests).isEmpty()
    }
}
