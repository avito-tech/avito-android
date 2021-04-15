package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property

public abstract class ExperimentalExtension {

    public abstract val useService: Property<Boolean>

    public abstract val useInMemoryReport: Property<Boolean>
}
