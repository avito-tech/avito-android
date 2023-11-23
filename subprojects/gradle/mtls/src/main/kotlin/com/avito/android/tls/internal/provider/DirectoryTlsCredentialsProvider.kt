package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import java.io.File

internal class DirectoryTlsCredentialsProvider(
    private val directory: File
) : TlsCredentialsProvider {

    override fun provideCredentials(): TlsCredentials {
        val files = directory
            .listFiles { file -> file.isKeyFile || file.isCrtFile }
            .orEmpty()

        val keyFiles = files.filter { it.isKeyFile }
        val crtFiles = files.filter { it.isCrtFile }

        if (keyFiles.size == 1 && crtFiles.size == 1) {
            return TlsCredentials.FileCredentials(crtFiles.first(), keyFiles.first())
        }

        return TlsCredentials.Undefined
    }
}

private val File.isKeyFile: Boolean get() = extension == "key"

private val File.isCrtFile: Boolean get() = extension == "crt"
