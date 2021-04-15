package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property

public abstract class ExperimentalExtension {

    /**
     * Enable experimental test run via Shared Build Service
     * https://docs.gradle.org/current/userguide/build_services.html
     */
    public abstract val useService: Property<Boolean>

    public abstract val useInMemoryReport: Property<Boolean>
}
