package com.avito.emcee.worker.internal.networking

import java.net.InetAddress
import java.net.URL

internal class WorkerHostAddressResolver {

    fun getWorkerRestUrl(workerPort: Int): URL {
        val host = InetAddress.getLocalHost().hostAddress
        return URL("http", host, workerPort, "/")
    }
}
