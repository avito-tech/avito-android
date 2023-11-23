package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials

internal class RawContentTlsCredentialsProvider(
    private val crtContent: String,
    private val keyContent: String,
) : TlsCredentialsProvider {

    override fun provideCredentials(): TlsCredentials {
        if (crtContent.isNotBlank() && keyContent.isNotBlank()) {
            return TlsCredentials.PlainCredentials(crtContent, keyContent)
        }

        return TlsCredentials.Undefined
    }
}
