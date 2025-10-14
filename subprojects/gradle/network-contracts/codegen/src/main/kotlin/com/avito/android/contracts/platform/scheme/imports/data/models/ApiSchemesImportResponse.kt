package com.avito.android.contracts.platform.scheme.imports.data.models

import kotlinx.serialization.Serializable
import java.nio.charset.Charset
import java.util.Base64

@Serializable
public data class ApiSchemeImportResponse(
    public val result: Schema
) {

    @Serializable
    public data class Schema(
        val schema: Map<String, String>? = null
    )
}

internal val ApiSchemeImportResponse.Schema.areSchemesExist: Boolean
    get() = !schema.isNullOrEmpty()

internal fun ApiSchemeImportResponse.Schema.entriesList(): List<SchemaEntry> {
    return schema?.map { SchemaEntry(it.key, it.value) }.orEmpty()
}

public data class SchemaEntry(
    val path: String,
    val content: String,
) {

    val decodedContent: String
        get() = Base64.getDecoder().decode(content).toString(Charset.defaultCharset())
}
