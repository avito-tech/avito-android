package com.avito.android

import org.gradle.api.provider.Property

public abstract class ArtifactoryExtension {

    public abstract val baseUrl: Property<String>

    public abstract val login: Property<String>

    public abstract val password: Property<String>

    /**
     * This parameters used to create path in artifactory repository
     * and differs from one that comes from json cd build config
     * There is no real reason of that
     *
     *  todo merge, take one from json
     */
    public abstract val backupRepository: Property<String>
    public abstract val projectName: Property<String>
    public abstract val projectType: Property<String>
    public abstract val version: Property<String>
}
