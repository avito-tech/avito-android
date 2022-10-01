package com.avito.emcee.worker.rest

import com.avito.emcee.queue.Bucket
import com.avito.emcee.worker.internal.rest.HttpServer
import com.avito.emcee.worker.internal.rest.handler.ProcessingBucketsRequestHandler
import com.avito.emcee.worker.internal.storage.SingleElementProcessingBucketsStorage
import com.avito.emcee.worker.rest.stub.stub
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.ServerSocket

@ExperimentalCoroutinesApi
class HttpServerTest {

    private var port: Int? = null
    private val bucketsStorage = SingleElementProcessingBucketsStorage()
    private val server = createServer()
    private val client = HttpClient(CIO)

    @BeforeEach
    fun beforeAll() {
        port = findAvailablePort()
        server.start(port!!)
    }

    @Test
    fun `processing buckets - returns correct result - when buckets list is empty`() = runTest {
        val response = client.post("http://127.0.0.1:$port/currentlyProcessingBuckets")
        assertEquals("{\"bucketIds\":[]}", response.bodyAsText())
    }

    @Test
    fun `processing buckets - returns correct result - when buckets list has entries`() = runTest {
        bucketsStorage.add(Bucket.stub("stub-bucket-id"))
        val response = client.post("http://127.0.0.1:$port/currentlyProcessingBuckets")
        assertEquals("{\"bucketIds\":[\"stub-bucket-id\"]}", response.bodyAsText())
    }

    @Test
    fun `unknown request - returns 400 Not Found`() = runTest {
        val response = client.post("http://127.0.0.1:$port/unknownRequest")
        assertEquals(404, response.status.value)
        assertEquals("Not Found", response.status.description)
    }

    @AfterEach
    fun after() {
        server.stop()
    }

    private fun findAvailablePort(): Int {
        val s = ServerSocket(0)
        val port = s.localPort
        s.close()
        return port
    }

    private fun createServer(): HttpServer = HttpServer.Builder()
        .addHandler(ProcessingBucketsRequestHandler(bucketsStorage))
        .debug(true)
        .build()
}
