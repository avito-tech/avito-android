package com.avito.android.gradle_configuration.extension.spec

import com.avito.android.gradle_configuration.extension.ArtifactV4
import com.avito.android.model.input.config.CdBuildConfigV4
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

public abstract class NupokatiV4PipelineSpec : BaseNupokatiPipelineSpec() {
    public abstract val cdBuildConfig: Property<CdBuildConfigV4>

    public abstract val versionCode: Property<Int>

    public abstract val nupokatiUrl: Property<String>
    public abstract val useTls: Property<Boolean>
    public abstract val artifacts: ListProperty<ArtifactV4>

    public abstract val chunkedUploadThresholdBytes: Property<Long>
    public abstract val nupokatiClientConnectionTimeout: Property<Long>
    public abstract val nupokatiClientReadTimeout: Property<Long>
    public abstract val nupokatiClientWriteTimeout: Property<Long>

    public override fun releaseVersion(): Provider<String> = cdBuildConfig.map { it.releaseVersion }
}
