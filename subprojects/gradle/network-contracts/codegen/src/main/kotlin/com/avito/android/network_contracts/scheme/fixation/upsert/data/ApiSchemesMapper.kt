package com.avito.android.network_contracts.scheme.fixation.upsert.data

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata
import com.avito.android.network_contracts.scheme.fixation.upsert.data.models.UpdateApiSchemesRequest

internal object ApiSchemesMapper {

    fun mapSchemesToRequest(
        author: String,
        version: String,
        schemes: List<ApiSchemesMetadata>,
    ): List<UpdateApiSchemesRequest> {
        val projectSchemes = schemes
            .groupBy({ it.projectName }, { it.schemes.pairs })
            .mapValues { it.value.flatten() }

        return projectSchemes
            .filter { (_, schemes) -> schemes.isNotEmpty() }
            .map { (projectName, schemes) ->
                UpdateApiSchemesRequest(
                    author = author,
                    version = version,
                    appName = projectName,
                    clientSchema = UpdateApiSchemesRequest.Schema(schemes.toMap())
                )
            }
    }
}

private val Map<String, String>.pairs: List<Pair<String, String>>
    get() {
        return entries.map { it.key to it.value }
    }
