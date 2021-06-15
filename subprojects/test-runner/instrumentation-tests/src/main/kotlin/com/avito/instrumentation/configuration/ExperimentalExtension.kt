package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property

public abstract class ExperimentalExtension {

    /**
     * Deprecated, not used anymore
     *
     * todo check avito usages and remove
     */
    public abstract val useService: Property<Boolean>

    public abstract val useInMemoryReport: Property<Boolean>

    /**
     * By default artifacts uploaded to file storage, and if file storage disabled you can't access it
     * This is not intended behavior and should be changed
     */
    public abstract val saveTestArtifactsToOutputs: Property<Boolean>

    public abstract val fetchLogcatForIncompleteTests: Property<Boolean>

    public abstract val uploadArtifactsFromRunner: Property<Boolean>
}
