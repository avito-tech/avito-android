package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class KubernetesViaContext @Inject constructor() : KubernetesExecutionEnvironment {

    public abstract val context: Property<String>
}
