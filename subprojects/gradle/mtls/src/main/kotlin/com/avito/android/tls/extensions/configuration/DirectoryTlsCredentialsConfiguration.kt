package com.avito.android.tls.extensions.configuration

import org.gradle.api.file.RegularFileProperty

/**
 * TLS credentials provider that retrieves credentials files from the directory.
 *
 * It assumes that the directory includes both .key and .crt files.
 * If there are multiple .key or .crt files in the directory, the first one encountered will be utilized.
 */
public interface DirectoryTlsCredentialsConfiguration : TlsCredentialsProviderConfiguration {

    public val directory: RegularFileProperty
}
