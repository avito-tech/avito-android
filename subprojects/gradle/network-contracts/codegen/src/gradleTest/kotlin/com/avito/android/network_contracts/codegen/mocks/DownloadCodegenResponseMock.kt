package com.avito.android.network_contracts.codegen.mocks

import okhttp3.mockwebserver.MockResponse
import okio.Buffer

internal fun downloadCodegenResponseMock(
    responseBody: Buffer
): MockResponse {
    return MockResponse()
        .setResponseCode(200)
        .setBody(responseBody)
}
