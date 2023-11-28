package com.avito.android.network_contracts.extension.urls

import org.gradle.api.provider.Property

public interface ArtifactoryUrlConfiguration : UrlConfiguration {

    public val artifactoryUrl: Property<String>
}
