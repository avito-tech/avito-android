package com.avito.android.gradle_configuration.extension.spec

import com.avito.android.gradle_configuration.extension.nested.ArtifactoryExtension
import com.avito.android.model.input.config.CdBuildConfigV2
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested

public abstract class NupokatiV2PipelineSpec : BaseNupokatiPipelineSpec() {
    public abstract val cdBuildConfig: Property<CdBuildConfigV2>

    public abstract val teamcityBuildUrl: Property<String>

    @get:Nested
    public abstract val artifactory: ArtifactoryExtension

    public fun artifactory(action: Action<ArtifactoryExtension>) {
        action.execute(artifactory)
    }

    public override fun releaseVersion(): Provider<String> = cdBuildConfig.map { it.releaseVersion }
}
