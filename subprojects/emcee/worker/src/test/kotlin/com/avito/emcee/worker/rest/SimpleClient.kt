package com.avito.emcee.worker.rest

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SimpleClient {

    private lateinit var clientSocket: Socket
    private lateinit var writer: PrintWriter
    private lateinit var reader: BufferedReader

    fun start(ip: String, port: Int) {
        clientSocket = Socket(ip, port)
        writer = PrintWriter(clientSocket.getOutputStream(), true)
        reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
    }

    fun sendRequest(body: String): String? {
        writer.println(body)
        return reader.readLine()
    }

    fun stop() {
        reader.close()
        writer.close()
        clientSocket.close()
    }
}
