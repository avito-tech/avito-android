package com.avito.emcee

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

public abstract class JobConfiguration {

    @get:Input
    public abstract val id: Property<String>

    @get:Input
    public abstract val groupId: Property<String>

    @get:Input
    public abstract val priority: Property<Int>

    @get:Input
    public abstract val groupPriority: Property<Int>
}
