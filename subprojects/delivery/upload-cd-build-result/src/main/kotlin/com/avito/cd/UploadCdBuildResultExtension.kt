package com.avito.cd

import org.gradle.api.provider.Property

public abstract class UploadCdBuildResultExtension {

    public abstract val artifactoryUser: Property<String>

    public abstract val artifactoryPassword: Property<String>

    public abstract val suppressFailures: Property<Boolean>
}
