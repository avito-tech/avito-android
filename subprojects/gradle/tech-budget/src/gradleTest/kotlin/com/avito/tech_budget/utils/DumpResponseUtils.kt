package com.avito.tech_budget.utils

import okhttp3.mockwebserver.MockResponse

internal fun successResponse() = MockResponse().setResponseCode(200).setBody(
    """
                {
                    "result": {
                        "id": "string"
                    }
                }
            """.trimIndent()
)

internal fun failureResponse() = MockResponse().setResponseCode(500).setBody(
    """
                {
                    "error": {
                        "kind": "validation",
                        "message": "string"
                    }
                }
            """.trimIndent()
)
