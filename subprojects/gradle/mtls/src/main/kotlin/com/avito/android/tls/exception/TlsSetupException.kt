package com.avito.android.tls.exception

internal class TlsSetupException(
    message: String
) : RuntimeException("Mtls setup exception: $message")
