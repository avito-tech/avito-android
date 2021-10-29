package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class KubernetesViaContext @Inject constructor() : ExecutionEnvironment {

    public abstract val context: Property<String>

    /**
     * we can get namespace from context, but using this value to check if expected context chosen
     * see [com.avito.k8s.KubernetesClientFactory]
     */
    public abstract val namespace: Property<String>
}
