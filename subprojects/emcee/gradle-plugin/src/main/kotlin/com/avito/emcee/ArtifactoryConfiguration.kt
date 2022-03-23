package com.avito.emcee

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

public abstract class ArtifactoryConfiguration {

    @get:Input
    public abstract val user: Property<String>

    @get:Input
    public abstract val password: Property<String>

    @get:Input
    public abstract val baseUrl: Property<String>

    @get:Input
    public abstract val repository: Property<String>
}
