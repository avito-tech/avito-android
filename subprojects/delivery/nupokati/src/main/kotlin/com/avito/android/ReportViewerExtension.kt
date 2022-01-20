package com.avito.android

import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.provider.Property

public abstract class ReportViewerExtension {

    public abstract val frontendUrl: Property<String>

    public abstract val reportCoordinates: Property<ReportCoordinates>
}
