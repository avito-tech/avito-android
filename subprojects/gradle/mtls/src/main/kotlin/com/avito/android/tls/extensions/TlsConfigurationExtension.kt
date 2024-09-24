package com.avito.android.tls.extensions

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

public abstract class TlsConfigurationExtension(
    objects: ObjectFactory
) {

    internal val credentials: TlsCredentialsRegister = TlsCredentialsRegister(objects)

    public val docsUrl: Property<String> = objects.property<String>()
        .convention("")

    public val verbose: Property<Boolean> = objects.property<Boolean>()
        .convention(false)

    public fun credentials(action: Action<TlsCredentialsRegister>) {
        action.execute(credentials)
    }
}
