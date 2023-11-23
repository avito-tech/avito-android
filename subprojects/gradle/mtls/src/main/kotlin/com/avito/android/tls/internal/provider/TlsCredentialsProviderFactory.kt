package com.avito.android.tls.internal.provider

import com.avito.android.tls.extensions.configuration.DirectoryTlsCredentialsConfiguration
import com.avito.android.tls.extensions.configuration.FilesTlsCredentialsConfiguration
import com.avito.android.tls.extensions.configuration.RawContentTlsCredentialsConfiguration
import com.avito.android.tls.extensions.configuration.TlsCredentialsProviderConfiguration

internal class TlsCredentialsProviderFactory {

    fun createProvider(configuration: TlsCredentialsProviderConfiguration): TlsCredentialsProvider {
        return when (configuration) {
            is RawContentTlsCredentialsConfiguration ->
                RawContentTlsCredentialsProvider(
                    crtContent = configuration.crtContent.get(),
                    keyContent = configuration.keyContent.get()
                )

            is FilesTlsCredentialsConfiguration ->
                FilesTlsCredentialsProvider(
                    crtFilePath = configuration.crtFilePath.get(),
                    keyFilePath = configuration.keyFilePath.get(),
                )

            is DirectoryTlsCredentialsConfiguration ->
                DirectoryTlsCredentialsProvider(
                    directory = configuration.directory.get().asFile,
                )
        }
    }
}
