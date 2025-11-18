package com.avito.android.gradle_configuration.extension.nested

import org.gradle.api.provider.Property

public abstract class ArtifactoryExtension {

    public abstract val login: Property<String>

    public abstract val password: Property<String>
}
