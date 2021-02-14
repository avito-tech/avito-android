package com.avito.android.build_checks.internal

import com.avito.android.build_checks.CheckMode
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

internal abstract class CheckTaskWithMode : DefaultTask() {

    @get:Input
    abstract val mode: Property<CheckMode>
}
