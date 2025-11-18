package com.avito.android.gradle_configuration.extension.spec

import com.avito.android.gradle_configuration.extension.nested.ArtifactoryExtension
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public abstract class NupokatiV2PipelineSpec : BaseNupokatiPipelineSpec() {

    public abstract val teamcityBuildUrl: Property<String>

    @get:Nested
    public abstract val artifactory: ArtifactoryExtension

    public fun artifactory(action: Action<ArtifactoryExtension>) {
        action.execute(artifactory)
    }
}
