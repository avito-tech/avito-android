package com.avito.android.mock

import java.net.InetAddress
import java.net.Socket

internal class FakeSocket constructor(
    private val localAddress: InetAddress,
    private val localPort: Int,
    private val inetAddress: InetAddress,
    private val port: Int
) : Socket() {

    constructor(inetAddress: InetAddress, localPort: Int) : this(
        inetAddress,
        localPort,
        inetAddress,
        1234
    )

    override fun getLocalAddress(): InetAddress = localAddress

    override fun getLocalPort(): Int = localPort

    override fun getInetAddress(): InetAddress = inetAddress

    override fun getPort(): Int = port
}
