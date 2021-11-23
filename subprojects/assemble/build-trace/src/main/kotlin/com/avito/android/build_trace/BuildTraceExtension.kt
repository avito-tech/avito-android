package com.avito.android.build_trace

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

public abstract class BuildTraceExtension {

    public abstract val enabled: Property<Boolean>

    /**
     * Output directory for reports.
     * build/reports/build-trace by default.
     */
    public abstract val output: DirectoryProperty
}
