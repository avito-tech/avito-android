package com.avito.android.network_contracts.scheme.imports.mocks

import okhttp3.mockwebserver.MockResponse

@Suppress("MaxLineLength")
internal fun apiSchemaImportResponseMock(
    path: String = "",
    filePaths: List<String> = emptyList()
): MockResponse {
    val files = filePaths.joinToString(
        separator = ",\n"
    ) { filePath ->
        """
            "$path/$filePath": "Y29tcG9uZW50czogc2NoZW1hczogaW5mbzogdGl0bGU6IF9fY29tcG9uZW50c19maWxlX18gdmVyc2lvbjogMS4wLjAgb3BlbmFwaTogMy4wLjAgcGF0aHM6IHt9Cg=="
        """.trimIndent()
    }

    val responseBody = """
        {
        "result": {
            "schema": {
                $files
            }
        }
    }
    """.trimIndent()
    return MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(responseBody)
}

internal fun apiSchemeEmptyResponse(): MockResponse {
    val responseBody = """
        {
            "result": {}
        }
    """.trimIndent()

    return MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(responseBody)
}
