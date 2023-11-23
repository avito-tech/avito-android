package com.avito.android.tls.extensions.configuration

import org.gradle.api.provider.Property

/**
 * TLS credentials provider that retrieves file paths for credentials from project properties.
 *
 * It assumes that file paths are provided as absolute paths.
 */
public interface FilesTlsCredentialsConfiguration : TlsCredentialsProviderConfiguration {

    public val crtFilePath: Property<String>

    public val keyFilePath: Property<String>
}
