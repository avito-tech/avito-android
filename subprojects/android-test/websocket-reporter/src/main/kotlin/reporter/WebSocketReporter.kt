package ru.avito.reporter

public interface WebSocketReporter {

    public fun onConnect(host: String)

    public fun onSend(message: String, enqueued: Boolean)

    public fun onReceive(message: String)

    public fun onError(message: String)

    public fun onClose()
}
