package com.avito.android.tls

import com.avito.android.tls.credentials.TlsCredentials
import com.avito.android.tls.extensions.configuration.TlsCredentialsProviderConfiguration
import org.gradle.api.provider.SetProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

public abstract class TlsCredentialsService : BuildService<TlsCredentialsService.Params> {

    public interface Params : BuildServiceParameters {
        public val configurations: SetProperty<TlsCredentialsProviderConfiguration>
    }

    private val factory: TlsProjectCredentialsFactory by lazy {
        TlsProjectCredentialsFactory(parameters.configurations.get())
    }

    public fun createCredentials(): TlsCredentials {
        return factory.createCredentials()
    }
}
