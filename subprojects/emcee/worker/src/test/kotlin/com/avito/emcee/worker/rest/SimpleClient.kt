package com.avito.emcee.worker.rest

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SimpleClient {

    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null

    fun start(ip: String, port: Int) {
        clientSocket = Socket(ip, port).also {
            writer = PrintWriter(it.getOutputStream(), true)
            reader = BufferedReader(InputStreamReader(it.getInputStream()))
        }
    }

    fun sendRequest(body: String): String? {
        requireNotNull(writer).println(body)
        return requireNotNull(reader).readLine()
    }

    fun stop() {
        reader?.close()
        writer?.close()
        clientSocket?.close()
    }
}
