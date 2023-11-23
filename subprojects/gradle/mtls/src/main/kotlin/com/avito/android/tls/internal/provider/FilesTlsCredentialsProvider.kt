package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import java.io.File

internal class FilesTlsCredentialsProvider(
    private val crtFilePath: String,
    private val keyFilePath: String,
) : TlsCredentialsProvider {

    override fun provideCredentials(): TlsCredentials {
        if (crtFilePath.isNotBlank() && keyFilePath.isNotBlank()) {
            val mtlsKeyFile = File(keyFilePath)
            val mtlsCrtFile = File(crtFilePath)

            if (mtlsCrtFile.exists() && mtlsKeyFile.exists()) {
                return TlsCredentials.FileCredentials(mtlsCrtFile, mtlsKeyFile)
            }
        }

        return TlsCredentials.Undefined
    }
}
