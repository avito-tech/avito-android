package com.avito.android.network_contracts.scheme.fixation.upsert.data

import com.avito.android.network_contracts.scheme.fixation.collect.ApiSchemesMetadata

internal object ApiSchemesMapper {

    fun <T> mapSchemesToRequest(
        schemes: List<ApiSchemesMetadata>,
        transform: (projectName: String, schemes: List<Pair<String, String>>) -> T
    ): List<T> {
        val projectSchemes = schemes
            .groupBy({ it.projectName }, { it.schemes.pairs })
            .mapValues { it.value.flatten() }

        return projectSchemes
            .filter { (_, schemes) -> schemes.isNotEmpty() }
            .map { (projectName, schemes) -> transform.invoke(projectName, schemes) }
    }
}

private val Map<String, String>.pairs: List<Pair<String, String>>
    get() {
        return entries.map { it.key to it.value }
    }
