package com.avito.android.tls

import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.extensions.configuration.TlsCredentialsProviderConfiguration
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public abstract class TlsCredentialsService : BuildService<TlsCredentialsService.Params> {

    public interface Params : BuildServiceParameters {
        public val configurations: SetProperty<TlsCredentialsProviderConfiguration>
        public val docsUrl: Property<String>
        public val verbose: Property<Boolean>
    }

    private val logger: Logger by lazy { Logging.getLogger("TlsCredentialsService") }

    private val factory: TlsProjectCredentialsFactory by lazy {
        TlsProjectCredentialsFactory(
            parameters.configurations.get(),
            parameters.docsUrl.get(),
            parameters.verbose.get(),
            logger
        )
    }

    public fun createCredentials(): TlsCredentials {
        return factory.createCredentials()
    }
}
