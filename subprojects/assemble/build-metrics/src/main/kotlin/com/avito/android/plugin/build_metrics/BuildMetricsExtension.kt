package com.avito.android.plugin.build_metrics

import org.gradle.api.provider.ListProperty

public abstract class BuildMetricsExtension {

    /**
     * Common prefix for statsd metrics
     */
    public abstract val metricsPrefix: ListProperty<String>
}
