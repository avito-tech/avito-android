package com.avito.android.tls.extensions

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory

public abstract class TlsConfigurationExtension(
    objects: ObjectFactory
) {

    internal val credentials: TlsCredentialsRegister = TlsCredentialsRegister(objects)

    public fun credentials(action: Action<TlsCredentialsRegister>) {
        action.execute(credentials)
    }
}
