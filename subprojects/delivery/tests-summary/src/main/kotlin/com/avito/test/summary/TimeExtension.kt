package com.avito.test.summary

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

/**
 * Team summaries are duplicated to Time when [token] is set,
 * for teams with `timeChannelId` in [AlertinoExtension.testSummaryDestination].
 */
public abstract class TimeExtension {

    @get:Input
    @get:Optional
    public abstract val baseUrl: Property<String>

    // Internal: a secret must not become a task input
    @get:Internal
    public abstract val token: Property<String>
}
