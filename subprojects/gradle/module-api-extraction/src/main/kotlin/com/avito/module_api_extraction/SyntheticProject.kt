package com.avito.module_api_extraction

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SyntheticProject(
    val sources: List<Source>
) {

    @JsonClass(generateAdapter = true)
    data class Source(
        val type: String,
        val relativePath: String,
        val className: String = "",
        val usedClasses: List<String> = emptyList()
    )
}
