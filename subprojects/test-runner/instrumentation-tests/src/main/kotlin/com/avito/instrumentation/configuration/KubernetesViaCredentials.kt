package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class KubernetesViaCredentials @Inject constructor() : ExecutionEnvironment {

    public abstract val token: Property<String>

    public abstract val caCertData: Property<String>

    public abstract val url: Property<String>

    public abstract val namespace: Property<String>
}
