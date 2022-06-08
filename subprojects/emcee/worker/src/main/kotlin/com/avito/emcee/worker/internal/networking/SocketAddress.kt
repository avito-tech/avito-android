package com.avito.emcee.worker.internal.networking

internal data class SocketAddress(val host: String, val port: Int) {

    fun serialized(): String = "$host:$port"
}
