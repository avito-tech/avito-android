package com.avito.emcee.worker.internal.identifier

import java.net.InetAddress

internal class HostnameWorkerIdProvider : WorkerIdProvider {

    override fun provide(): String {
        return InetAddress.getLocalHost().hostName
    }
}
