package com.avito.emcee.worker.internal.rest

import com.avito.emcee.worker.internal.rest.handler.RequestHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

internal class HttpServer(private val handlers: List<RequestHandler>, private val debug: Boolean) {

    private var serverThread: Thread? = null

    @Volatile
    private var isStopped = false

    fun start(port: Int) {
        serverThread = Thread {
            val ss = ServerSocket(port)
            if (debug) println("Starting REST server on $port port/")

            ss.use {
                while (!Thread.interrupted()) {
                    val connection = ss.accept()
                    val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
                    val writer = PrintWriter(connection.getOutputStream())

                    try {
                        val request = reader.readLine() ?: continue

                        val body = StringBuilder()
                        while (true) {
                            val line = reader.readLine()
                            if (line == null || line.isEmpty()) break
                            body.append(line).append("\n")
                        }

                        if (debug) println("<-- $request\n$body")

                        val handler = findHandler(request)
                        val response = if (handler != null) {
                            handler.response().wrapToSuccessfulResponse()
                        } else {
                            badRequest()
                        }
                        if (debug) println("--> $response")

                        writer.write(response)
                    } finally {
                        writer.close()
                        reader.close()
                        connection.close()
                    }
                }
            }
        }.apply { start() }
    }

    fun stop() {
        require(!isStopped) { "Server has not been started" }
        isStopped = true
    }

    private fun findHandler(request: String): RequestHandler? {
        val parts = request.split(" ")
        val method = HttpMethod.valueOf(parts[0])
        val path = parts[1]
        return handlers.find { it.method == method && it.path == path }
    }

    class Builder {

        private val handlers: MutableList<RequestHandler> = mutableListOf()
        private var debug = false

        fun addHandler(handler: RequestHandler) = apply {
            handlers.add(handler)
        }

        fun debug(value: Boolean): Builder = apply {
            debug = value
        }

        fun build(): HttpServer = HttpServer(handlers, debug)
    }
}
