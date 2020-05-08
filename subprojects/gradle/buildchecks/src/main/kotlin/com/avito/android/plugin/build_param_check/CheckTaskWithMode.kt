package com.avito.android.plugin.build_param_check

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

internal abstract class CheckTaskWithMode : DefaultTask() {

    @get:Input
    abstract val mode: Property<CheckMode>

}
