package com.avito.android.critical_path

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

public abstract class CriticalPathExtension {

    public abstract val enabled: Property<Boolean>

    /**
     * Output directory for reports.
     * build/reports/critical-path by default.
     */
    public abstract val output: DirectoryProperty
}
