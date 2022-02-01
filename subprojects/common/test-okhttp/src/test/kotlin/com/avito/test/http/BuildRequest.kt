package com.avito.test.http

import okhttp3.Headers
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import java.net.InetAddress
import java.nio.charset.Charset

internal fun buildRequest(
    method: String = "GET",
    path: String = "",
    body: String? = null
): RecordedRequest =
    RecordedRequest(
        requestLine = "$method /$path HTTP/1.1",
        headers = Headers.Builder().build(),
        chunkSizes = emptyList(),
        bodySize = if (body == null) {
            -1
        } else {
            Buffer().writeString(
                body,
                Charset.forName("UTF-8")
            ).size
        },
        body = if (body == null) Buffer() else Buffer().writeString(body, Charset.forName("UTF-8")),
        sequenceNumber = -1,
        socket = StubSocket(
            inetAddress = InetAddress.getByAddress(
                "127.0.0.1",
                byteArrayOf(127, 0, 0, 1)
            ),
            localPort = 80
        )
    )
