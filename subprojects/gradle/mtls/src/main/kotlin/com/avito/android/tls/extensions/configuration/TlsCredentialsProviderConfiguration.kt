package com.avito.android.tls.extensions.configuration

import org.gradle.api.Named
import org.gradle.api.provider.Property

public sealed interface TlsCredentialsProviderConfiguration : Named {

    public val helperText: Property<String>
}
