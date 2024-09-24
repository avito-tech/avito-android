package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import java.io.File

internal class FilesTlsCredentialsProvider(
    private val crtFilePath: String,
    private val keyFilePath: String,
) : TlsCredentialsProvider {

    override fun provideCredentials(): TlsCredentials {
        return when {
            crtFilePath.isNotBlank() && keyFilePath.isNotBlank() -> defineFileCredentials()

            crtFilePath.isBlank() && keyFilePath.isBlank() ->
                TlsCredentials.Undefined

            crtFilePath.isBlank() -> TlsCredentials.NotFound(
                message = "No crt file path specified"
            )

            keyFilePath.isBlank() -> TlsCredentials.NotFound(
                message = "No key file path specified"
            )

            else -> TlsCredentials.Undefined
        }
    }

    private fun defineFileCredentials(): TlsCredentials {
        val mtlsKeyFile = File(keyFilePath)
        val mtlsCrtFile = File(crtFilePath)

        return when {
            mtlsCrtFile.exists() && mtlsKeyFile.exists() -> TlsCredentials.FileCredentials(
                tlsCrtFile = mtlsCrtFile,
                tlsKeyFile = mtlsKeyFile
            )

            mtlsCrtFile.exists() && !mtlsKeyFile.exists() -> TlsCredentials.NotFound(
                message = "Unable to find specified mTLS key file with path: $keyFilePath"
            )

            mtlsKeyFile.exists() && !mtlsCrtFile.exists() -> TlsCredentials.NotFound(
                message = "Unable to find specified mTLS crt file with path: $crtFilePath"
            )

            else -> TlsCredentials.NotFound(
                message = "Unable to find specified mTLS files with paths: crt: $crtFilePath, key: $keyFilePath"
            )
        }
    }
}
