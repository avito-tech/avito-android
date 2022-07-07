package com.avito.emcee.worker.rest

import com.avito.emcee.worker.internal.rest.HttpServer
import com.avito.emcee.worker.internal.rest.handler.ProcessingBucketsRequestHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HttpServerTest {

    private val server: HttpServer = HttpServer.Builder()
        .addHandler(ProcessingBucketsRequestHandler())
        .debug(false)
        .build()
    private val client: SimpleClient = SimpleClient()
    private val defaultPort = 8080

    @BeforeEach
    fun before() {
        server.start(defaultPort)
        client.start("127.0.0.1", defaultPort)
    }

    @Test
    fun `http server - returns 200 OK - when request is supported`() {
        val response = client.sendRequest("""
            |POST /currentlyProcessingBuckets HTTP/1.0
            |Content-Type: application-json
            |
            |{"param":1}
        """.trimMargin())
        assertEquals(response, "HTTP/1.0 200 OK")
    }

    @Test
    fun `http server - returns 400 Bad Request - when request is unknown`() {
        val response = client.sendRequest("""
            |POST /unknownRequest HTTP/1.0
            |Content-Type: application-json
            |
            |{"param":1}
        """.trimMargin())
        assertEquals(response, "HTTP/1.0 400 Bad Request")
    }

    @AfterEach
    fun after() {
        server.stop()
        client.stop()
    }
}
