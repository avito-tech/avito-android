package com.avito.android.build_trace

import org.gradle.api.provider.Property

public abstract class BuildTraceExtension {

    public abstract val enabled: Property<Boolean>
}
