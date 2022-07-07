package com.avito.emcee.worker.internal.networking

import java.net.InetAddress

internal class SocketAddressResolver {

    fun resolve(port: Int): SocketAddress {
        return SocketAddress(
            host = InetAddress.getLocalHost().hostName,
            port = port
        )
    }
}
