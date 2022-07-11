package com.avito.test.summary

import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

public abstract class ReportViewerExtension {

    @get:Input
    public abstract val url: Property<String>

    @get:Input
    public abstract val reportsHost: Property<String>

    @get:Input
    public abstract val reportCoordinates: Property<ReportCoordinates>
}
