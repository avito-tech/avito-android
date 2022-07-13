package com.avito.emcee.worker.internal.rest

import com.avito.emcee.worker.internal.rest.handler.RequestHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

internal class HttpServer(
    private val handlers: List<RequestHandler>,
    private val debug: Boolean
) {

    private var serverSocket: ServerSocket? = null

    @Volatile
    internal var isStarted: Boolean = false

    fun start(port: Int) {
        if (debug) println("Starting REST server on $port port/")
        serverSocket = ServerSocket(port)
        isStarted = true
        while (true) {
            ClientHandler(
                clientSocket = serverSocket!!.accept(),
                debug = debug,
                handlers = handlers
            ).start()
        }
    }

    fun stop() {
        serverSocket?.close()
        isStarted = false
    }

    @Suppress("IfThenToElvis")
    private class ClientHandler(
        private val clientSocket: Socket,
        private val debug: Boolean,
        private val handlers: List<RequestHandler>
    ) : Thread() {

        override fun run() {
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val writer = PrintWriter(clientSocket.getOutputStream())

            try {
                val request = reader.readLine() ?: return

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
                clientSocket.close()
            }
        }

        private fun findHandler(request: String): RequestHandler? {
            val parts = request.split(" ")
            val method = HttpMethod.valueOf(parts[0])
            val path = parts[1]
            return handlers.find { it.method == method && it.path == path }
        }
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
