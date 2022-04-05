package com.avito.instrumentation.configuration

import org.gradle.api.provider.Property

public abstract class ExperimentalExtension {

    /**
     * Deprecated, not used anymore
     *
     * todo check avito usages and remove
     */
    public abstract val useService: Property<Boolean>

    /**
     * Deprecated, not used anymore
     *
     * todo check avito usages and remove
     */
    public abstract val useInMemoryReport: Property<Boolean>

    /**
     * By default artifacts uploaded to file storage, and if file storage disabled you can't access it
     * This is not intended behavior and should be changed
     */
    public abstract val saveTestArtifactsToOutputs: Property<Boolean>

    public abstract val uploadArtifactsFromRunner: Property<Boolean>

    public abstract val useLegacyExtensionsV1Beta: Property<Boolean>

    // TODO: enable after fixing "Pod requests queue is empty", see MBS-11776 comments
    public abstract val sendPodsMetrics: Property<Boolean>
}
