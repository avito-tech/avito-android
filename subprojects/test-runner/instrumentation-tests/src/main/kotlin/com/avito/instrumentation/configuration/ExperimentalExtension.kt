package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property

public abstract class ExperimentalExtension {

    /**
     * Enable experimental test run via Shared Build Service
     * https://docs.gradle.org/current/userguide/build_services.html
     */
    public abstract val useService: Property<Boolean>

    public abstract val useInMemoryReport: Property<Boolean>

    /**
     * By default artifacts uploaded to file storage, and if file storage disabled you can't access it
     * This is not intended behavior and should be changed
     */
    public abstract val saveTestArtifactsInOutputs: Property<Boolean>

    public abstract val fetchLogcatForIncompleteTests: Property<Boolean>
}
