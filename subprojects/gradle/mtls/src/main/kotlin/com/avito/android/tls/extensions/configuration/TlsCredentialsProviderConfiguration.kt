package com.avito.android.tls.extensions.configuration

import org.gradle.api.Named
import org.gradle.api.provider.Property

public sealed interface TlsCredentialsProviderConfiguration : Named {

    @Deprecated("Unused anymore")
    public val helperText: Property<String>

    /**
     * Text that will be shown in the error message when the credentials are not found.
     * It should contains action that configuration does.
     * Example: Searching for .crt and .key files in the directory:
     */
    public val actionText: Property<String>

    /**
     * The text that will be shown in the error message when credentials are not found, as a possible solution.
     * Example:
     */
    public val fallbackText: Property<String>
}
