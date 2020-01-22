package ru.avito.reporter

interface WebSocketReporter {

    fun onConnect(host: String)

    fun onSend(message: String, enqueued: Boolean)

    fun onReceive(message: String)

    fun onError(message: String)

    fun onClose()
}
