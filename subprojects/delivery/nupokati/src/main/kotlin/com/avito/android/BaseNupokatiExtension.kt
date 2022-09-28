package com.avito.android

import com.avito.android.model.input.CdBuildConfigParserFactory
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested

public abstract class BaseNupokatiExtension {

    @get:Nested
    public abstract val artifactory: ArtifactoryExtension

    @get:Nested
    public abstract val reportViewer: ReportViewerExtension

    public abstract val cdBuildConfigFile: RegularFileProperty

    public abstract val teamcityBuildUrl: Property<String>

    public fun artifactory(action: Action<ArtifactoryExtension>) {
        action.execute(artifactory)
    }

    public fun reportViewer(action: Action<ReportViewerExtension>) {
        action.execute(reportViewer)
    }

    public fun releaseVersion(): Provider<String> {
        return cdBuildConfigFile.map(CdBuildConfigParserFactory()).map { it.releaseVersion }
    }
}
