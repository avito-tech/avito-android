package com.avito.android.build_metrics

import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Internal

public interface HasBuildMetricsTags {
    @get:Internal
    public val buildMetricsTags: MapProperty<String, String>
}
