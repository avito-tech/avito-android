package com.avito.android.contracts.network.scheme.upsert.mocks

import com.avito.android.contracts.platform.scheme.imports.data.models.SchemaEntry

internal fun generateExpectedJson(
    moduleNotation: String,
    author: String = "test-client",
    appName: String = "avito-app",
    version: String = "develop",
    schemes: List<SchemaEntry> = emptyList()
): String {
    val schemesString = schemes.joinToString(
        separator = ","
    ) {
        "\"deps/$moduleNotation/${it.path}\":\"${it.content}\""
    }
    return """
        {
           "author": "$author",
           "appName": "$appName",
           "version": "$version",
           "clientSchema": {
                schema: {
                    $schemesString
                }
           }
        }
    """.trimIndent()
}
