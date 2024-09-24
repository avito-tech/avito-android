package com.avito.android.tls.extensions

import com.avito.android.tls.extensions.configuration.TlsCredentialsProviderConfiguration
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.model.ObjectFactory

public class TlsCredentialsRegister(
    objects: ObjectFactory
) {

    internal val tlsCredentialsProviders:
        ExtensiblePolymorphicDomainObjectContainer<TlsCredentialsProviderConfiguration> = objects
            .polymorphicDomainObjectContainer(TlsCredentialsProviderConfiguration::class.java)

    public fun <T : TlsCredentialsProviderConfiguration> registerProvider(
        name: String,
        cls: Class<T>,
        configuration: Action<T>
    ): NamedDomainObjectProvider<T> {
        return tlsCredentialsProviders.register(name, cls, configuration)
    }
}
