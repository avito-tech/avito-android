package com.avito.android.gradle_configuration.extension.spec

import org.gradle.api.Named
import org.gradle.api.provider.Provider

public interface NupokatiPipelineSpec : Named {
    public fun releaseVersion(): Provider<String>
}
