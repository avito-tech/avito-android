package com.avito.android.tech_budget

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.property

public abstract class DumpInfoConfiguration(
    objects: ObjectFactory
) {

    @get:Input
    public abstract val commitHash: Property<String>

    @get:Input
    public abstract val currentDate: Property<String>

    @get:Input
    public abstract val project: Property<String>

    @get:Input
    public abstract val baseUploadUrl: Property<String>

    @get:Input
    public val useTls: Property<Boolean> = objects.property<Boolean>().convention(true)
}
