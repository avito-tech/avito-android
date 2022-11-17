package com.avito.android.tech_budget

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

public abstract class DumpInfoConfiguration {

    @get:Input
    public abstract val commitHash: Property<String>

    @get:Input
    public abstract val currentDate: Property<String>

    @get:Input
    public abstract val project: Property<String>

    @get:Input
    public abstract val baseUploadUrl: Property<String>
}
