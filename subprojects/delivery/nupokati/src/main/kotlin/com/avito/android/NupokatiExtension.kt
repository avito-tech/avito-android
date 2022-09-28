package com.avito.android

import org.gradle.api.provider.Property

public abstract class NupokatiExtension : BaseNupokatiExtension() {

    public abstract val releaseBuildVariantName: Property<String>
}
