package com.avito.emcee.worker.rest

import com.avito.emcee.worker.internal.rest.HttpServer
import com.avito.emcee.worker.internal.rest.handler.ProcessingBucketsRequestHandler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.util.concurrent.Executors

class HttpServerTest {

    private var port: Int? = null
    private val client = SimpleClient()
    private val server = createServer()

    @BeforeEach
    fun before() {
        port = findAvailablePort()
        Executors.newSingleThreadExecutor().submit { server.start(port!!) }
        waitServerStart()

        client.start("127.0.0.1", port!!)
    }

    @Test
    fun `http server - returns 200 OK - when request is supported`() {

        val response = client.sendRequest(
            """
            |POST /currentlyProcessingBuckets HTTP/1.0
            |Content-Type: application-json
            |
            |{"param":1}
        """.trimMargin()
        )
        assertEquals(response, "HTTP/1.0 200 OK")
    }

    @Test
    fun `http server - returns 400 Bad Request - when request is unknown`() {
        client.start("127.0.0.1", port!!)
        val response = client.sendRequest(
            """
            |POST /unknownRequest HTTP/1.0
            |Content-Type: application-json
            |
            |{"param":1}
        """.trimMargin()
        )
        assertEquals(response, "HTTP/1.0 400 Bad Request")
    }

    @AfterEach
    fun after() {
        client.stop()
        server.stop()
    }

    private fun waitServerStart() {
        while (!server.isStarted) {
            Thread.onSpinWait()
        }
    }

    private fun findAvailablePort(): Int {
        val s = ServerSocket(0)
        val port = s.localPort
        s.close()
        return port
    }

    private fun createServer(): HttpServer = HttpServer.Builder()
        .addHandler(ProcessingBucketsRequestHandler(NoOpProcessingBucketsStorage()))
        .debug(true)
        .build()
}
