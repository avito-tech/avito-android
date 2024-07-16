package com.avito.test.summary

import org.gradle.api.Named
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import javax.inject.Inject

public abstract class TestSummaryAppExtension @Inject constructor() : Named {

    @get:Nested
    public abstract val alertino: AlertinoExtension

    @get:Nested
    public abstract val reportViewer: ReportViewerExtension

    @get:Input
    public abstract val buildUrl: Property<String>

    @get:Input
    public abstract val currentBranch: Property<String>
}
