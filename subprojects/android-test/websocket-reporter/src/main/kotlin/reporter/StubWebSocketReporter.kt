package ru.avito.reporter

public class StubWebSocketReporter : WebSocketReporter {

    override fun onConnect(host: String) {
    }

    override fun onSend(message: String, enqueued: Boolean) {
    }

    override fun onReceive(message: String) {
    }

    override fun onError(message: String) {
    }

    override fun onClose() {
    }
}
