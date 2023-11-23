package com.avito.android.tls.extensions.configuration

import org.gradle.api.provider.Property

/**
 * TLS credentials provider that retrieves raw credentials content from project properties.
 *
 * It assumes that the file contents are non-empty.
 */
public interface RawContentTlsCredentialsConfiguration : TlsCredentialsProviderConfiguration {

    public val crtContent: Property<String>

    public val keyContent: Property<String>
}
