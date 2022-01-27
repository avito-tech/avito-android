package com.avito.android

import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class NupokatiExtension {

    @get:Nested
    public abstract val artifactory: ArtifactoryExtension

    @get:Nested
    public abstract val googlePlay: GooglePlayExtensions

    @get:Nested
    public abstract val reportViewer: ReportViewerExtension

    public abstract val cdBuildConfigFile: RegularFileProperty

    public abstract val teamcityBuildUrl: Property<String>

    public abstract val releaseBuildVariantName: Property<String>

    /**
     * disable manually for tests or uploading from service
     */
    public abstract val uploadCrashlyticsMapping: Property<Boolean>

    public fun artifactory(action: Action<ArtifactoryExtension>) {
        action.execute(artifactory)
    }

    public fun googlePlay(action: Action<GooglePlayExtensions>) {
        action.execute(googlePlay)
    }

    public fun reportViewer(action: Action<ReportViewerExtension>) {
        action.execute(reportViewer)
    }
}
