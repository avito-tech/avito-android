package com.avito.android.tls.internal.provider

import com.avito.android.tls.credentials.TlsCredentials
import java.io.File

internal class DirectoryTlsCredentialsProvider(
    private val directory: File,
) : TlsCredentialsProvider {

    override fun provideCredentials(): TlsCredentials {
        if (!directory.exists()) {
            return TlsCredentials.Undefined
        }

        val files = directory
            .listFiles { file -> file.isKeyFile || file.isCrtFile }
            .orEmpty()
            .sortedWith(compareBy<File> { it.name }.thenBy { it.extension })

        val keyFiles = files.filter { it.isKeyFile }
        val crtFiles = files.filter { it.isCrtFile }

        return when {
            keyFiles.isEmpty() && crtFiles.isEmpty() -> TlsCredentials.Undefined

            keyFiles.isEmpty() || crtFiles.isEmpty() -> TlsCredentials.NotFound(
                "No key or crt files found in directory: ${directory.absolutePath}"
            )

            keyFiles.size > 1 || crtFiles.size > 1 -> TlsCredentials.NotFound(
                message = """
                    |Multiple key and/or crt files found in directory: ${directory.absolutePath}.
                    |Files:${files.joinToString(prefix = FILES_SEPARATOR, separator = FILES_SEPARATOR) { it.path }}
                """.trimMargin(marginPrefix = "|"),
            )

            else -> TlsCredentials.FileCredentials(
                crtFiles.first(),
                keyFiles.first()
            )
        }
    }

    private companion object {

        private const val FILES_SEPARATOR = "\n|\t- "
    }
}

private val File.isKeyFile: Boolean get() = extension == "key"

private val File.isCrtFile: Boolean get() = extension == "crt"
