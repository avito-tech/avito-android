package com.avito.android

import com.avito.android.model.CdBuildConfig
import com.avito.reportviewer.model.ReportCoordinates
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class NupokatiExtension {

    @get:Nested
    public abstract val artifactory: ArtifactoryExtension

    public abstract val suppressFailures: Property<Boolean>

    public abstract val teamcityBuildUrl: Property<String>

    public abstract val releaseBuildVariantName: Property<String>

    public abstract val googlePlayTrack: Property<CdBuildConfig.Deployment.Track>

    public abstract val reportViewerUrl: Property<String>

    public abstract val reportCoordinates: Property<ReportCoordinates>
}
