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

        return when {
            crtContent.isNotBlank() && keyContent.isNotBlank() ->
                TlsCredentials.PlainCredentials(crtContent, keyContent)

            crtContent.isBlank() && keyContent.isBlank() ->
                TlsCredentials.Undefined

            crtContent.isBlank() -> TlsCredentials.NotFound(
                message = "No crt content specified"
            )

            keyContent.isBlank() -> TlsCredentials.NotFound(
                message = "No key content specified"
            )

            else -> TlsCredentials.Undefined
        }
    }
}
