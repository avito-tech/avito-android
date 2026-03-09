package com.avito.android.gradle_configuration.extension.spec

import com.avito.android.DEFAULT_RELEASE_VARIANT
import com.avito.android.gradle_configuration.extension.nested.ArtifactoryExtension
import com.avito.android.model.input.config.CdBuildConfigV2
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested
import org.gradle.kotlin.dsl.property

public abstract class NupokatiV2PipelineSpec(
    objects: ObjectFactory,
) : BaseNupokatiPipelineSpec() {
    public abstract val cdBuildConfig: Property<CdBuildConfigV2>

    public abstract val teamcityBuildUrl: Property<String>

    @get:Nested
    public abstract val artifactory: ArtifactoryExtension

    public val releaseVariant: Property<String> =
        objects.property<String>().convention(DEFAULT_RELEASE_VARIANT)

    public fun artifactory(action: Action<ArtifactoryExtension>) {
        action.execute(artifactory)
    }

    public override fun releaseVersion(): Provider<String> = cdBuildConfig.map { it.releaseVersion }
}
