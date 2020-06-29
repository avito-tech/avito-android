package com.avito.android.mock

import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer

@Deprecated(
    "use same class from common module",
    replaceWith = ReplaceWith("com.avito.test.http.RequestData")
)
class RequestData(private val recordedRequest: RecordedRequest) {

    val path: String = recordedRequest.path ?: ""

    val body: String by lazy {
        val copyBody = Buffer()
        recordedRequest.body.copyTo(copyBody, 0, recordedRequest.bodySize)
        copyBody.readUtf8()
    }
}
